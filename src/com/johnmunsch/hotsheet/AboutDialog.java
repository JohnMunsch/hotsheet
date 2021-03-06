/*
 * AboutDialog.java
 *
 * Created on March 31, 2001, 11:41 AM
 */
package com.johnmunsch.hotsheet;

/**
 *
 * @author  John Munsch
 */
class AboutDialog extends JCenterableDialog {
    public AboutDialog(java.awt.Frame parent, boolean modal) {
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
        jLabel1 = new javax.swing.JLabel();

        setTitle("About HotSheet 0.94 alpha");
        setModal(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/johnmunsch/hotsheet/resources/SplashScreen.jpg")));
        getContentPane().add(jLabel1, java.awt.BorderLayout.CENTER);

        pack();
    }//GEN-END:initComponents

    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
}
