/*
 * SubscriptionsDialog.java
 *
 * Created on March 5, 2002, 12:59 PM
 */
package com.johnmunsch.hotsheet;

/**
 *
 * @author  JMunsch
 */
public class SubscriptionsDialog extends JCenterableDialog {
    /** Creates new form SubscriptionsDialog */
    public SubscriptionsDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        
        center(VS_PARENT);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel3 = new javax.swing.JPanel();
        jList3 = new javax.swing.JList();
        jPanel4 = new javax.swing.JPanel();
        jList2 = new javax.swing.JList();
        jPanel5 = new javax.swing.JPanel();
        jList1 = new javax.swing.JList();
        jPanel2 = new javax.swing.JPanel();
        OKButton = new javax.swing.JButton();
        CancelButton = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jCheckBox1 = new javax.swing.JCheckBox();
        jPanel8 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        jPanel3.setLayout(new java.awt.BorderLayout());

        jList3.setPreferredSize(new java.awt.Dimension(350, 250));
        jPanel3.add(jList3, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab("All", jPanel3);

        jPanel4.setLayout(new java.awt.BorderLayout());

        jPanel4.add(jList2, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab("Subscribed", jPanel4);

        jPanel5.setLayout(new java.awt.BorderLayout());

        jPanel5.add(jList1, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab("New", jPanel5);

        getContentPane().add(jTabbedPane1, java.awt.BorderLayout.CENTER);

        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        OKButton.setText("OK");
        jPanel2.add(OKButton);

        CancelButton.setText("Cancel");
        jPanel2.add(CancelButton);

        getContentPane().add(jPanel2, java.awt.BorderLayout.SOUTH);

        jPanel6.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel1.setText("Filter:");
        jPanel6.add(jLabel1);

        jTextField1.setMinimumSize(new java.awt.Dimension(150, 20));
        jTextField1.setPreferredSize(new java.awt.Dimension(200, 20));
        jPanel6.add(jTextField1);

        jCheckBox1.setText("Filter on descriptions");
        jPanel6.add(jCheckBox1);

        getContentPane().add(jPanel6, java.awt.BorderLayout.NORTH);

        jPanel7.setLayout(new java.awt.GridLayout(3, 0, 5, 0));

        jButton1.setText("Subscribe");
        jPanel7.add(jButton1);

        jButton2.setText("Unsubscribe");
        jPanel7.add(jButton2);

        jButton3.setText("Get List");
        jPanel7.add(jButton3);

        jPanel8.add(jPanel7);

        getContentPane().add(jPanel8, java.awt.BorderLayout.EAST);

        pack();
    }//GEN-END:initComponents
    
    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JButton jButton3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton OKButton;
    private javax.swing.JButton CancelButton;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JList jList3;
    private javax.swing.JList jList2;
    private javax.swing.JList jList1;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
}