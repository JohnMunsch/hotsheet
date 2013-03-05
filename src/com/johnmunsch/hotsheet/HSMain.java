/*
 * HSMain.java
 *
 * Created on March 16, 2001, 7:23 PM
 */
package com.johnmunsch.hotsheet;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.*;
import javax.jnlp.*;
import javax.swing.*;
import javax.swing.event.*;

import com.ibm.bsf.*;
import edu.stanford.ejalbert.BrowserLauncher;
import gui.JMouseWheelFrame;
import org.apache.log4j.*;

import com.johnmunsch.rss.*;

/**
 *
 * @author  John Munsch
 */
public class HSMain extends JMouseWheelFrame {
    private static HSMain main = null;

    private ChannelStore channelStore = new ListChannelStore();
    private ItemStore itemStore = new ListItemStore();
    private ItemHistory itemHistory = new HashMapItemHistory();
    private HSItemStoreListener itemStoreListener = new HSItemStoreListener();
    private HSChannelStoreListener channelStoreListener = 
        new HSChannelStoreListener(itemStore);
    
    private DefaultListModel listModel = new DefaultListModel();
    private String label = "";
    private int range = 0;
    private int value = 0;
    private boolean labelAndProgressDirty = false;
    private java.util.Timer timer = new java.util.Timer();
    private BSFManager bsfManager = new BSFManager();
    private JFileChooser fileChooser = new JFileChooser(
        new WindowsAltFileSystemView());

    private static Category cat = Category.getInstance(HSMain.class.getName());

    class RefreshActionListener implements ActionListener {
        Channel channelToRefresh;

        RefreshActionListener(Channel channel) {
            channelToRefresh = channel;
        }

        public void actionPerformed(java.awt.event.ActionEvent evt) {
            refreshOneChannel(channelToRefresh);
        }
    }

    /** Creates new form HSMain */
    public HSMain() {
        main = this;

        // Configure the log4j appender, layout, etc.
        PropertyConfigurator.configure(HSSettings.getInstance().getProperties());
        
        // Build the main user interface.
        initComponents();
        
        // Set the list model for the item list.
        itemList.setModel(listModel);
        
        // We can go ahead and show our splash screen while we are performing 
        // our initialization. It'll give the users something to look at.
        JSplash splash = new JSplash(this, 
            "/com/johnmunsch/hotsheet/resources/SplashScreen.jpg", 2000); 
        splash.show(); 

        // Critical - this focus request must be here after 
        // showing the parent to get keystrokes properly.
        splash.requestFocus(); 

        startup();
    }

    private void startup() {
        HSSettings settings = HSSettings.getInstance();

        // The channel store loads its current set of channels. If no channel
        // list is found then a default set of channels will be loaded into the
        // list.
        channelStore.setItemHistory(itemHistory);
        channelStore.load(settings.getChannelStoreFile());
        itemStore.load(settings.getItemStoreFile());
        itemHistory.load(settings.getItemHistoryFile());

        // Set up the listeners.
        itemStore.addItemStoreListener(itemStoreListener);
        channelStore.addChannelStoreListener(channelStoreListener);

        try {
            bsfManager.declareBean("channelStore", channelStore, ChannelStore.class);
            bsfManager.declareBean("itemStore", itemStore, ItemStore.class);
        } catch (BSFException bsfe) {
            cat.error("Unable to declare beans for scripting due to " + bsfe);
        }
        
        // Normally Java Web Start handles a proxy for us, even if it is a proxy
        // that requires authentication. However, if HotSheet is run from the
        // command line the proxy has to be set manually so we have properties
        // to set us up for proxy operation (though it is not good enough for
        // an authenticated proxy.
        //
        // Note that the default value of this is false so unless the user sets
        // something it will not be used.
	boolean useProxy = new Boolean(
            settings.getProperties().getProperty("http.proxySet", 
            "false")).booleanValue();

	if (useProxy) {
            System.setProperty("http.proxySet", "true");
            System.setProperty("http.proxyHost", 
                settings.getProperties().getProperty("http.proxyHost"));
            System.setProperty("http.proxyPort", 
                settings.getProperties().getProperty("http.proxyPort"));

            // See if we have an authentication name for authenticating proxies.
            boolean useAuth = new Boolean(
                settings.getProperties().getProperty("http.authSet", 
                "false")).booleanValue();

            if (useAuth) {
                final String authenticationName = 
                    settings.getProperties().getProperty("http.authName");
                final String authenticationPassword = 
                    settings.getProperties().getProperty("http.authPass");

                try {
                    //registering with the system.
                    Authenticator.setDefault(new Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(
                                authenticationName, 
                                authenticationPassword.toCharArray());
                        }
                    });
                } catch (SecurityException se) {
                    cat.error("Security exception while registering Authenticator with system: ", 
                        se);
                }
            }
        }
        
        // If the user wants to have refresh occur automatically then the number
        // of seconds to wait between refreshes will be set in the properties
        // file.
        int refreshInterval = (new Integer(
            settings.getProperties().getProperty("refresh.interval", 
            "0"))).intValue();

        if (refreshInterval > 0) {
            timer.scheduleAtFixedRate(new TimerTask() {
                public void run() {
                    channelStore.retrieve(true);
                }
            }, refreshInterval * 1000, refreshInterval * 1000);
        }
        
        // If the item store is empty query the user about going ahead and doing
        // a refresh now.
        if (itemStore.size() == 0) {
            int answer = JOptionPane.showConfirmDialog(this, 
                "Would you like to refresh all channels? (New HotSheet users should always say yes)",
                "Refresh Now?", JOptionPane.YES_NO_OPTION);

            if (answer == JOptionPane.YES_OPTION) {
                refreshAllChannels();
            }
        } else {
            updateItemList();
        }
    }
    
    private void shutdown() {
        HSSettings settings = HSSettings.getInstance();

	boolean deleteViewed = new Boolean(
            settings.getProperties().getProperty("delete.viewed", 
            "false")).booleanValue();

        // If the flag is set we delete all the viewed items on exit.
        if (deleteViewed) {
            deleteViewed();
        }

        channelStore.store(settings.getChannelStoreFile());
        itemStore.store(settings.getItemStoreFile());
        itemHistory.store(settings.getItemHistoryFile());

        // Read the current size and location of the frame window and store it
        // in the settings before we write them out.
        Dimension size = getSize();
        Point location = getLocation();
        
        Properties properties = HSSettings.getInstance().getProperties();
        properties.put("width", new Integer(size.width).toString());
        properties.put("height", new Integer(size.height).toString());
        properties.put("left", new Integer(location.x).toString());
        properties.put("top", new Integer(location.y).toString());
        
        settings.store();
    }

    public void updateItemList() {
        listModel.clear();

        // Load up the list with all of the items we currently have in our
        // item store.
        Iterator i = itemStore.iterator();
        
        while (i.hasNext()) {
            Item item = (Item) i.next();
            
            listModel.addElement(item);
        }
        
        jLabel2.setText(new Integer(itemStore.size()).toString());
    }

    public void updateStatusAndProgress() {
        if (labelAndProgressDirty) {
            statusLabel.setText(label);

            if (range != progressBar.getMaximum()) {
                progressBar.setMaximum(range);
            }

            progressBar.setValue(value);
        }
    }

    public void setLabel(String _label) {
        label = _label;
        labelAndProgressDirty = true;
    }
    
    public void setRange(int _range) {
        range = _range;
        labelAndProgressDirty = true;
    }
    
    public void setValue(int _value) {
        value = _value;
        labelAndProgressDirty = true;
    }
    
    public static HSMain getInstance() {
        return (main);
    }

    // Normally this function would be marked and wouldn't be editable within
    // NetBeans. But unfortunately I managed to delete my .form file somewhere
    // along the way. Unless I recreate it I will have to do future edits to the
    // main interface manually.
    private void initComponents() {
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        preferencesMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        exitMenuItem = new javax.swing.JMenuItem();
        newsMenu = new javax.swing.JMenu();
        refreshAllMenuItem = new javax.swing.JMenuItem();
        refreshAllPopupMenuItem = new javax.swing.JMenuItem();
        refreshMenu = new javax.swing.JMenu();
        refreshPopupMenu = new javax.swing.JMenu();
        jSeparator3 = new javax.swing.JSeparator();
        jSeparator4 = new javax.swing.JSeparator();
        removeSelectedMenuItem = new javax.swing.JMenuItem();
        removeSelectedPopupMenuItem = new javax.swing.JMenuItem();
        removeViewedMenuItem = new javax.swing.JMenuItem();
        removeViewedPopupMenuItem = new javax.swing.JMenuItem();
        removeAllMenuItem = new javax.swing.JMenuItem();
        removeAllPopupMenuItem = new javax.swing.JMenuItem();
        ViewMenu = new javax.swing.JMenu();
        ArrangeItemsMenu = new javax.swing.JMenu();
        ChannelTitleMenuItem = new javax.swing.JMenuItem();
        ItemTitleMenuItem = new javax.swing.JMenuItem();
        ScoreMenuItem = new javax.swing.JMenuItem();
        ViewedMenuItem = new javax.swing.JMenuItem();
        DateRetrievedMenuItem = new javax.swing.JMenuItem();
        scriptMenu = new javax.swing.JMenu();
        runScriptMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        aboutMenuItem = new javax.swing.JMenuItem();
        jToolBar1 = new javax.swing.JToolBar();
        refreshAllButton = new javax.swing.JButton();
        removeAllButton = new javax.swing.JButton();
        preferencesButton = new javax.swing.JButton();
        aboutButton = new javax.swing.JButton();
        itemListScrollPane = new javax.swing.JScrollPane();
        itemList = new javax.swing.JList();
        popupMenu = new javax.swing.JPopupMenu();
        jPanel1 = new javax.swing.JPanel();
        statusLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        jLabel2 = new javax.swing.JLabel();
        
        fileMenu.setText("File");
        fileMenu.setMnemonic('f');
        
        preferencesMenuItem.setText("Preferences...");
        preferencesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                preferencesMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(preferencesMenuItem);
        
        fileMenu.add(jSeparator1);
        
        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenuItem);
        menuBar.add(fileMenu);
        
        newsMenu.setText("News");
        newsMenu.setMnemonic('n');

        refreshAllMenuItem.setText("Refresh All Channels");
        refreshAllMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 
            0));
        refreshAllMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshAllMenuItemActionPerformed(evt);
            }
        });
        newsMenu.add(refreshAllMenuItem);

        refreshMenu.setText("Refresh");
        newsMenu.add(refreshMenu);
        newsMenu.add(jSeparator3);
        refreshAllPopupMenuItem.setText("Refresh All Channels");
        refreshAllPopupMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshAllMenuItemActionPerformed(evt);
            }
        });
        popupMenu.add(refreshAllPopupMenuItem);
        
        refreshPopupMenu.setText("Refresh");
        popupMenu.add(refreshPopupMenu);
        popupMenu.add(jSeparator4);
        removeSelectedMenuItem.setText("Remove Selected Items");
        removeSelectedMenuItem.setAccelerator(KeyStroke.getKeyStroke(
            KeyEvent.VK_DELETE, 0));
        removeSelectedMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeSelectedMenuItemActionPerformed(evt);
            }
        });
        newsMenu.add(removeSelectedMenuItem);

        removeSelectedPopupMenuItem.setText("Remove Selected Items");
        removeSelectedPopupMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeSelectedMenuItemActionPerformed(evt);
            }
        });
        popupMenu.add(removeSelectedPopupMenuItem);
        
        removeViewedMenuItem.setText("Remove Viewed Items");
        removeViewedMenuItem.setAccelerator(KeyStroke.getKeyStroke(
            KeyEvent.VK_DELETE, java.awt.Event.SHIFT_MASK));
        removeViewedMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeViewedMenuItemActionPerformed(evt);
            }
        });
        newsMenu.add(removeViewedMenuItem);
        
        removeViewedPopupMenuItem.setText("Remove Viewed Items");
        removeViewedPopupMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeViewedMenuItemActionPerformed(evt);
            }
        });
        popupMenu.add(removeViewedPopupMenuItem);

        removeAllMenuItem.setText("Remove All Items");
        removeAllMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeAllMenuItemActionPerformed(evt);
            }
        });
        newsMenu.add(removeAllMenuItem);

        removeAllPopupMenuItem.setText("Remove All Items");
        removeAllPopupMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeAllMenuItemActionPerformed(evt);
            }
        });
        popupMenu.add(removeAllPopupMenuItem);

        refreshMenu.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuCanceled(MenuEvent e) {
            }
            
            public void menuDeselected(MenuEvent e) {
                refreshMenu.removeAll();
            }

            public void menuSelected(MenuEvent e) {
                // If there are no available channels then we need to disable 
                // the menu item.
                if (channelStore.size() == 0) {
                    refreshMenu.setEnabled(false);
                } else {
                    // Fill in the menu based upon the user's channel selections.
                    Iterator i = channelStore.iterator();

                    while (i.hasNext()) {
                        Channel channel = (Channel) i.next();
                        JMenuItem newItem = null;
                        
                        if (channel.getTitle().equals("")) {
                            newItem = refreshMenu.add(channel.getURI());
                        } else {
                            newItem = refreshMenu.add(channel.getTitle());
                        }
                        
                        newItem.addActionListener(new RefreshActionListener(channel));
                    }
                }
            }
        });

        refreshPopupMenu.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuCanceled(MenuEvent e) {
            }
            
            public void menuDeselected(MenuEvent e) {
                refreshPopupMenu.removeAll();
            }

            public void menuSelected(MenuEvent e) {
                // If there are no available channels then we need to disable 
                // the menu item.
                if (channelStore.size() == 0) {
                    refreshPopupMenu.setEnabled(false);
                } else {
                    // Fill in the menu based upon the user's channel selections.
                    Iterator i = channelStore.iterator();

                    while (i.hasNext()) {
                        Channel channel = (Channel) i.next();
                        JMenuItem newItem = null;
                        
                        if (channel.getTitle().equals("")) {
                            newItem = refreshPopupMenu.add(channel.getURI());
                        } else {
                            newItem = refreshPopupMenu.add(channel.getTitle());
                        }
                        
                        newItem.addActionListener(new RefreshActionListener(channel));
                    }
                }
            }
        });
        
        newsMenu.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuCanceled(MenuEvent e) {
            }
            
            public void menuDeselected(MenuEvent e) {
            }

            public void menuSelected(MenuEvent e) {
                if (listModel.getSize() == 0) {
                    removeSelectedMenuItem.setEnabled(false);
                    removeViewedMenuItem.setEnabled(false);
                    removeAllMenuItem.setEnabled(false);
                } else {
                    if (!itemList.isSelectionEmpty()) {
                        removeSelectedMenuItem.setEnabled(true);
                    } else {
                        removeSelectedMenuItem.setEnabled(false);
                    }
                    
                    if (haveViewedItems()) {
                        removeViewedMenuItem.setEnabled(true);
                    } else {
                        removeViewedMenuItem.setEnabled(false);
                    }

                    removeAllMenuItem.setEnabled(true);
                }
            }
        });

        popupMenu.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(PopupMenuEvent evt) {
            }
            
            public void popupMenuWillBecomeInvisible(PopupMenuEvent evt) {
            }

            public void popupMenuWillBecomeVisible(PopupMenuEvent evt) {
                if (listModel.getSize() == 0) {
                    removeSelectedPopupMenuItem.setEnabled(false);
                    removeViewedPopupMenuItem.setEnabled(false);
                    removeAllPopupMenuItem.setEnabled(false);
                } else {
                    if (!itemList.isSelectionEmpty()) {
                        removeSelectedPopupMenuItem.setEnabled(true);
                    } else {
                        removeSelectedPopupMenuItem.setEnabled(false);
                    }
                    
                    if (haveViewedItems()) {
                        removeViewedPopupMenuItem.setEnabled(true);
                    } else {
                        removeViewedPopupMenuItem.setEnabled(false);
                    }
                    
                    removeAllPopupMenuItem.setEnabled(true);
                }
            }
        });
        menuBar.add(newsMenu);

        ViewMenu.setText("View");
        ViewMenu.setMnemonic('v');

        ArrangeItemsMenu.setText("Arrange Items");
        ChannelTitleMenuItem.setText("by Channel Title");
        ChannelTitleMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ChannelTitleMenuItemActionPerformed(evt);
            }
        });
        ArrangeItemsMenu.add(ChannelTitleMenuItem);

        ItemTitleMenuItem.setText("by Item Title");
        ItemTitleMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ItemTitleMenuItemActionPerformed(evt);
            }
        });
        ArrangeItemsMenu.add(ItemTitleMenuItem);

        DateRetrievedMenuItem.setText("by Date Retrieved");
        DateRetrievedMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DateRetrievedMenuItemActionPerformed(evt);
            }
        });
        ArrangeItemsMenu.add(DateRetrievedMenuItem);

        ScoreMenuItem.setText("by Score");
        ScoreMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ScoreMenuItemActionPerformed(evt);
            }
        });
        ArrangeItemsMenu.add(ScoreMenuItem);

        ViewedMenuItem.setText("by Viewed Status");
        ViewedMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ViewedMenuItemActionPerformed(evt);
            }
        });
        ArrangeItemsMenu.add(ViewedMenuItem);

        ViewMenu.add(ArrangeItemsMenu);
        menuBar.add(ViewMenu);

//        scriptMenu.setText("Script");
//        scriptMenu.setMnemonic('s');

//        runScriptMenuItem.setText("Run Script...");
//        runScriptMenuItem.setEnabled(true);
//        runScriptMenuItem.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                runScriptMenuItemActionPerformed(evt);
//            }
//        });
//        scriptMenu.add(runScriptMenuItem);
//        menuBar.add(scriptMenu);
        
        helpMenu.setText("Help");
        helpMenu.setMnemonic('h');

        aboutMenuItem.setText("About HotSheet...");
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(aboutMenuItem);
        menuBar.add(helpMenu);
        
        setTitle("HotSheet");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });
        
        refreshAllButton.setToolTipText("Refresh All Channels");
        refreshAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshAllButtonActionPerformed(evt);
            }
        });

        refreshAllButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/johnmunsch/hotsheet/resources/Refresh24.gif")));
        jToolBar1.add(refreshAllButton);
        
        preferencesButton.setToolTipText("Preferences...");
        preferencesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                preferencesButtonActionPerformed(evt);
            }
        });
        
        preferencesButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/johnmunsch/hotsheet/resources/Preferences24.gif")));
        jToolBar1.add(preferencesButton);
        
        jToolBar1.add(javax.swing.Box.createHorizontalStrut(10));

        removeAllButton.setToolTipText("Remove All Items");
        removeAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeAllButtonActionPerformed(evt);
            }
        });
        
        removeAllButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/johnmunsch/hotsheet/resources/Delete24.gif")));
        jToolBar1.add(removeAllButton);
        
        jToolBar1.add(javax.swing.Box.createHorizontalStrut(10));
        
        aboutButton.setToolTipText("About HotSheet...");
        aboutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutButtonActionPerformed(evt);
            }
        });
        
        aboutButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/johnmunsch/hotsheet/resources/About24.gif")));
        jToolBar1.add(aboutButton);
        
        getContentPane().add(jToolBar1, java.awt.BorderLayout.NORTH);
        
        itemList.setCellRenderer(new ItemListCellRenderer());
        itemList.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                itemListKeyPressed(evt);
            }
        });
        
        itemList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                itemListMouseClicked(evt);
            }

            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (evt.isPopupTrigger()) {
                    popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
                }
            }
            
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                if (evt.isPopupTrigger()) {
                    popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
                }
            }
        });

        itemListScrollPane.setViewportView(itemList);

        getContentPane().add(itemListScrollPane, java.awt.BorderLayout.CENTER);

        // Set up the status bar at the bottom of the window.
        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.X_AXIS));
        
        statusLabel.setText("");
        statusLabel.setPreferredSize(new java.awt.Dimension(100, 21));
        statusLabel.setBorder(new javax.swing.border.BevelBorder(javax.swing.border.BevelBorder.LOWERED));
        statusLabel.setMaximumSize(new java.awt.Dimension(32000, 21));
        jPanel1.add(statusLabel);
        
        progressBar.setPreferredSize(new java.awt.Dimension(100, 21));
        progressBar.setBorder(new javax.swing.border.BevelBorder(javax.swing.border.BevelBorder.LOWERED));
        progressBar.setMinimumSize(new java.awt.Dimension(10, 21));
        progressBar.setMaximumSize(new java.awt.Dimension(100, 21));
        jPanel1.add(progressBar);
        
        jLabel2.setText("");
        jLabel2.setPreferredSize(new java.awt.Dimension(70, 21));
        jLabel2.setBorder(new javax.swing.border.BevelBorder(javax.swing.border.BevelBorder.LOWERED));
        jLabel2.setMinimumSize(new java.awt.Dimension(70, 21));
        jLabel2.setMaximumSize(new java.awt.Dimension(70, 21));
        jLabel2.setHorizontalAlignment(SwingConstants.CENTER);
        jPanel1.add(jLabel2);
        
        getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);
        
        setJMenuBar(menuBar);
        pack();
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        Properties properties = HSSettings.getInstance().getProperties();
        int width = new Integer(properties.getProperty("width", "400")).intValue();
        int height = new Integer(properties.getProperty("height", "400")).intValue();
        int left = new Integer(properties.getProperty("left", 
            new Integer((screenSize.width - width) / 2).toString())).intValue();
        int top = new Integer(properties.getProperty("top", 
            new Integer((screenSize.height - height) / 2).toString())).intValue();
        setSize(new java.awt.Dimension(width, height));
        setLocation(left, top);
    }

    private void itemListKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_itemListKeyPressed
        if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
            browseToSelected();
        }
    }//GEN-LAST:event_itemListKeyPressed

    private void removeAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeAllButtonActionPerformed
        removeAllMenuItemActionPerformed(evt);
    }//GEN-LAST:event_removeAllButtonActionPerformed

    private void refreshAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshAllButtonActionPerformed
        refreshAllMenuItemActionPerformed(evt);
    }//GEN-LAST:event_refreshAllButtonActionPerformed

    private void preferencesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_preferencesButtonActionPerformed
        preferencesMenuItemActionPerformed(evt);
    }//GEN-LAST:event_preferencesButtonActionPerformed

    private void aboutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutButtonActionPerformed
        aboutMenuItemActionPerformed(evt);
    }//GEN-LAST:event_aboutButtonActionPerformed

    private void removeSelectedMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeSelectedMenuItemActionPerformed
        // First we update the model based on the user's action.
        Object[] selected = itemList.getSelectedValues();

        // Save off the new index we intend to use after we are done deleting
        // items.
        int newIndex = 0;
        if (selected.length > 0) {
            newIndex = Math.max(((itemList.getSelectedIndices())[0]) - 1, 0);
        }
        
        for (int i = 0; i < selected.length; i++) {
            itemStore.remove((Item) selected[i]);
        }
        
        // Then we update the view based upon the model.
        updateItemList();

        // Move the selection point to a reasonable new spot (either before or
        // after the deleted selection).
        itemList.setSelectedIndex(newIndex);
    }//GEN-LAST:event_removeSelectedMenuItemActionPerformed

    private void removeViewedMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        deleteViewed();

        // Update the view based upon the updated model.
        updateItemList();
    }
    
    private void removeAllMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeAllMenuItemActionPerformed
        itemStore.clear();
        
        updateItemList();
    }//GEN-LAST:event_removeAllMenuItemActionPerformed

    private void itemListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_itemListMouseClicked
        if (evt.getClickCount() == 2) {
            browseToSelected();
        }
    }//GEN-LAST:event_itemListMouseClicked

    private void preferencesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_preferencesMenuItemActionPerformed
        PreferencesDialog preferencesDialog = new PreferencesDialog(this, true, 
            channelStore, itemHistory);
        preferencesDialog.show();
        
        if (preferencesDialog.returnValue == PreferencesDialog.OK_OPTION) {
            preferencesDialog.loadChannelStore();
        }
    }//GEN-LAST:event_preferencesMenuItemActionPerformed

    private void DateRetrievedMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        itemStore.sort(ItemStore.ITEM_RETRIEVAL_DATE, 
            ItemStore.NONE, ItemStore.NONE);
        
        updateItemList();
    }

    private void ItemTitleMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        itemStore.sort(ItemStore.ITEM_TITLE, 
            ItemStore.NONE, ItemStore.NONE);
        
        updateItemList();
    }

    private void ChannelTitleMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        itemStore.sort(ItemStore.CHANNEL_TITLE, ItemStore.NONE, 
            ItemStore.NONE);
        
        updateItemList();
    }

    private void ScoreMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        itemStore.sort(ItemStore.SCORE, ItemStore.NONE, 
            ItemStore.NONE);
        
        updateItemList();
    }

    private void ViewedMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        itemStore.sort(ItemStore.VIEWED, ItemStore.NONE, 
            ItemStore.NONE);
        
        updateItemList();
    }

    private void runScriptMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        String filename = null;
        
        if (fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        
        File file = fileChooser.getSelectedFile();
        filename = file.toString();
        
        try {
            String language = bsfManager.getLangFromFilename(filename);
            BufferedReader scriptReader = new BufferedReader(
                new FileReader(fileChooser.getSelectedFile()));
            String temp = new String();
            String script = new String();
            while ((temp = scriptReader.readLine()) != null) {
                script += temp + "\n";
            }
            bsfManager.exec(language, filename, 0, 0, script);
        } catch (BSFException bsfe) {
            cat.error("Failed to execute the script " + filename + 
                " because of exception ", bsfe);
        } catch (java.io.FileNotFoundException fnfe) {
            cat.error("Unable to find the file " + filename);
        } catch (java.io.IOException ioe) {
            cat.error("Error reading " + filename);
        }
    }

    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
        new AboutDialog(this, true).show();
    }//GEN-LAST:event_aboutMenuItemActionPerformed

    private void refreshAllMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshAllMenuItemActionPerformed
        refreshAllChannels();
    }//GEN-LAST:event_refreshAllMenuItemActionPerformed

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        shutdown();

        System.exit(0);
    }//GEN-LAST:event_exitMenuItemActionPerformed

    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        shutdown();

        System.exit(0);
    }//GEN-LAST:event_exitForm

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        main = new HSMain();
        main.show();
    }

    ////////////////////////////////////////////////////////////////////////////
    //                                                          Helper Functions
    ////////////////////////////////////////////////////////////////////////////
    /**
     *
     */
    public void allowRefreshing(boolean allow) {
        if (allow) {
            refreshAllMenuItem.setEnabled(true);
            refreshAllPopupMenuItem.setEnabled(true);
            refreshMenu.setEnabled(true);
            refreshPopupMenu.setEnabled(true);
            refreshAllButton.setEnabled(true);
        } else {
            refreshAllMenuItem.setEnabled(false);
            refreshAllPopupMenuItem.setEnabled(false);
            refreshMenu.setEnabled(false);
            refreshPopupMenu.setEnabled(false);
            refreshAllButton.setEnabled(false);
        }
    }

    private void refreshAllChannels() {
        allowRefreshing(false);
        
        channelStore.retrieve(true);
    }        

    private void refreshOneChannel(final Channel channel) {
        allowRefreshing(false);
        
        List itemList = channel.retrieve(true);

        itemStore.add(itemList);
            
        updateItemList();

        allowRefreshing(true);
    }

    private void deleteViewed() {
        // Go through the list of items and remove those which have been viewed.
        Iterator iterator = itemStore.iterator();

        while (iterator.hasNext()) {
            Item item = (Item) iterator.next();
            
            if (item.getProperty("viewed") != null) {
                iterator.remove();
            }
        }
    }

    private void browseToSelected() {
        int selectedIndex = itemList.getSelectedIndex();

        if (selectedIndex != -1) {
            Item item = (Item) listModel.elementAt(selectedIndex);

            showURL(item.getLink());
            item.setProperty("viewed", "1");
        }
    }

    /**
     * Tries to load a url using the services provided by Java Web Start. If 
     * they aren't available then it falls back to an internal function.
     */
    private boolean showURL(URL url) {
        try { 
            // Lookup the javax.jnlp.BasicService object 
            BasicService bs = 
                (BasicService) ServiceManager.lookup("javax.jnlp.BasicService");
           
           // Invoke the showDocument method 
           return (bs.showDocument(url));
        } catch(UnavailableServiceException ue) {
            // Convert the URL to a string and call BrowserLauncher to try and
            // load the address.
            try {
                BrowserLauncher.openURL(url.toExternalForm());

                return (true);
            } catch (java.io.IOException ioe) {
                cat.error(ioe);
                return (false);
            }
        }
    }

    /**
     * Tells whether or not there are viewed items in the current list of items.
     */
    private boolean haveViewedItems() {
        Iterator i = itemStore.iterator();
        
        while (i.hasNext()) {
            Item item = (Item) i.next();
            
            if (item.getProperty("viewed") != null) {
                return true;
            }
        }
        
        return false;
    }
    
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenuItem preferencesMenuItem;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu newsMenu;
    private javax.swing.JMenuItem refreshAllMenuItem;
    private javax.swing.JMenu refreshMenu;
    private javax.swing.JMenuItem refreshAllPopupMenuItem;
    private javax.swing.JMenu refreshPopupMenu;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JMenuItem markSelectedAsRead;
    private javax.swing.JMenuItem markSelectedAsUnread;
    private javax.swing.JMenuItem removeSelectedMenuItem;
    private javax.swing.JMenuItem removeSelectedPopupMenuItem;
    private javax.swing.JMenuItem removeViewedMenuItem;
    private javax.swing.JMenuItem removeViewedPopupMenuItem;
    private javax.swing.JMenuItem removeAllMenuItem;
    private javax.swing.JMenuItem removeAllPopupMenuItem;
    private javax.swing.JMenu ViewMenu;
    private javax.swing.JMenu ArrangeItemsMenu;
    private javax.swing.JMenuItem DateRetrievedMenuItem;
    private javax.swing.JMenuItem ChannelTitleMenuItem;
    private javax.swing.JMenuItem ItemTitleMenuItem;
    private javax.swing.JMenuItem ScoreMenuItem;
    private javax.swing.JMenuItem ViewedMenuItem;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenu scriptMenu;
    private javax.swing.JMenuItem runScriptMenuItem;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JButton refreshAllButton;
    private javax.swing.JButton removeAllButton;
    private javax.swing.JButton preferencesButton;
    private javax.swing.JButton aboutButton;
    private javax.swing.JScrollPane itemListScrollPane;
    private javax.swing.JList itemList;
}
