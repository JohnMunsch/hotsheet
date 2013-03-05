/*
 * ItemListCellRenderer.java
 *
 * Created on April 2, 2001, 10:42 AM
 */
package com.johnmunsch.hotsheet;

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.lang.Math;
import java.text.*;
import java.util.Random;
import java.util.Vector;
import javax.swing.border.Border;
import javax.swing.ImageIcon;
import javax.swing.ListCellRenderer;
import javax.swing.JList;
import javax.swing.UIManager;

import com.johnmunsch.rss.*;

class ItemComponent extends Component {
    protected Item item;
    protected boolean isSelected, hasFocus, drawImages, isViewed;
    protected int score;

    ItemComponent(Item _item, boolean _isSelected, boolean _hasFocus, 
        boolean _drawImages, boolean _isViewed, int _score) {

        item = _item;
        isSelected = _isSelected;
        hasFocus = _hasFocus;
        drawImages = _drawImages;
        isViewed = _isViewed;
        score = _score;

        if (isSelected) {
            // Draw the item using a different background color.
            setBackground(UIManager.getColor("List.selectionBackground"));
        } else {
            setBackground(UIManager.getColor("List.background"));
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    //                                                          Helper Functions
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Calculates any scaling and/or translations that may need to be done to a
     * given image in order to display it centered within an area of the given
     * size.
     */
    protected AffineTransform createTransforms(Image image, Dimension dimension) {
        int imageHeight = image.getHeight(null);
        int imageWidth = image.getWidth(null);
        AffineTransform returnValue = new AffineTransform();

        if ((imageHeight > dimension.getHeight()) || 
            (imageWidth > dimension.getWidth())) {

            // Scaling will be necessary.
            double scaleFactor = Math.min(dimension.getHeight() / imageHeight, 
                dimension.getWidth() / imageWidth);
            
            // Concatenate a scaling transform on.
            AffineTransform temp = new AffineTransform();
            temp.setToScale(scaleFactor, scaleFactor);
            returnValue.concatenate(temp);
            
            // Recalculate the image height and width based on the scale factor.
            // We need to do this because we might need to horizontally or
            // vertically center the image.
            imageHeight *= scaleFactor;
            imageWidth *= scaleFactor;
        }
        
        // Note that it is possible to need both scaling and centering so we
        // always do our scaling first and then after we know our final
        // dimensions for the image we do our centering.
        if ((imageHeight < dimension.getHeight()) || 
            (imageWidth < dimension.getWidth())) {
            
            // Centering will be necessary.
            AffineTransform temp = new AffineTransform();
            temp.setToTranslation((dimension.getWidth() - imageWidth) / 
                2, (dimension.getHeight() - imageHeight) / 2);
            returnValue.concatenate(temp);
        }
        
        return returnValue;
    }
}

class LargeItemComponent extends ItemComponent {
    private final int margin = 8;
    private final int height = 100;
    private final int maxIconWidth = 140;
    private final int maxIconHeight = height - (margin * 2);

    LargeItemComponent(Item _item, boolean _isSelected, boolean _hasFocus, 
        boolean _drawImages, boolean _isViewed, int _score) {
        
        super(_item, _isSelected, _hasFocus, _drawImages, _isViewed, _score);
    }

    public void paint(Graphics g) {

        AttributedString str = null;
        AttributedCharacterIterator strIterator = null;
        FontRenderContext fontRenderContext = null;
        LineBreakMeasurer measurer = null;
        TextLayout layout = null;
        Point2D.Float penPosition = null;
        float wrappingWidth;
        float curY;
        
        int textMargin = maxIconWidth + (margin * 2);

        Graphics2D g2 = (Graphics2D) g;

        g2.setBackground(getBackground());
        g2.clearRect(0, 0, getWidth(), getHeight());
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
            RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, 
            RenderingHints.VALUE_RENDER_QUALITY);

        if (hasFocus) {
            // Draw a selection border.
            Border focusBorder =
                UIManager.getBorder("List.focusCellHighlightBorder");

            focusBorder.paintBorder(this, g2, 0, 0, getWidth(), getHeight());
        }

        // Draw the icon for the channel of the item.
        ImageIcon imageIcon = item.getChannel().getImageIcon();

        if (drawImages) {
            if (imageIcon != null) {
                AffineTransform transform = createTransforms(
                    imageIcon.getImage(), new Dimension(maxIconWidth, 
                    maxIconHeight));

                AffineTransform temp = new AffineTransform();
                temp.setToTranslation(margin, margin);
                transform.concatenate(temp);
                g2.drawImage(imageIcon.getImage(), transform, 
                    (ImageObserver) null);
            }
        } else {
            textMargin = margin;
        }

        // Draw the title of the item.
        g2.setColor(Color.black);
        Font currentFont = g2.getFont();
        Font boldFont = currentFont.deriveFont(Font.BOLD);

	String title = item.getTitle().trim();
        wrappingWidth = getWidth() - textMargin;
        curY = margin;

        if (!title.equals("")) {
            str = new AttributedString(title);
            str.addAttribute(TextAttribute.FONT, boldFont);
            strIterator = str.getIterator();
            fontRenderContext = new FontRenderContext(null,
                    false, false);
            measurer = new LineBreakMeasurer(strIterator,
                fontRenderContext);

            while (measurer.getPosition() < strIterator.getEndIndex()) {
                layout = measurer.nextLayout(wrappingWidth);
                curY += Math.floor(layout.getAscent());
                penPosition = new Point2D.Float(textMargin, curY);
                layout.draw(g2, penPosition.x, penPosition.y);
                curY += layout.getDescent() + layout.getLeading();
                if (curY >= getHeight()-3) {
                        break;
                }
            }
        }

        // Draw the description of the item.
        Font smallFont = currentFont.deriveFont(10.0f);

        String description = item.getDescription().trim();
        if (!description.equals("")) {
            description = description.replace('\n', ' ');
            str = new AttributedString(description);
            str.addAttribute(TextAttribute.FONT, smallFont);
            strIterator = str.getIterator();
            fontRenderContext = new FontRenderContext(null,
                false, false);
            measurer = new LineBreakMeasurer(strIterator,
                fontRenderContext);

            while (measurer.getPosition() < strIterator.getEndIndex()) {
                layout = measurer.nextLayout(wrappingWidth);

                // Adjust current elevation.
                curY += Math.floor(layout.getAscent());

                penPosition = new Point2D.Float(textMargin, curY);
                layout.draw(g2, penPosition.x, penPosition.y);

                // Move to next line.
                curY += layout.getDescent() + layout.getLeading();

                // If we've done enough lines to fill the printable area, don't
                // bother getting more.
                if (curY >= getHeight()-3) {
                    break;
                }
            }
        }

        if (score > 50) {
            score = Math.min(score, 100);
            g2.setColor(new Color(255, 255 - ((score - 50) * 5), 
                255 - ((score - 50) * 5)));
            g2.fillRect(0, 0, margin / 2, getHeight());
        } else if (score < 50) {
            score = Math.max(score, 0);
            g2.setColor(new Color(255 - ((50 - score) * 5), 
                255 - ((50 - score) * 5), 255));
            g2.fillRect(0, 0, margin / 2, getHeight());
        }
            
	g2.setColor(Color.lightGray);
	g2.fillRect(0, getHeight() - 3, getWidth(), getHeight());

        // If the item has already been viewed, draw a film over the whole
        // item to render it much subtler.
        if (isViewed) {
            AlphaComposite ac =
                AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5f);
            g2.setComposite(ac);

            // The background color is what we use to ghost the item.
            g2.setColor(getBackground());
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    public Dimension getPreferredSize() {
        return (new Dimension(300, height));
    }
}

/**
 *
 * @author John Munsch
 */
public class ItemListCellRenderer implements ListCellRenderer {
    private static boolean drawImages = true;
    
    public ItemListCellRenderer() {
        drawImages = new Boolean(
            HSSettings.getInstance().getProperties().getProperty(
            "draw.channelImages", "true")).booleanValue();
    }
    
    public Component getListCellRendererComponent(JList list, Object value,
        int index, boolean isSelected, boolean cellHasFocus) {

        Item item = (Item) value;

        return (new LargeItemComponent(item, isSelected, cellHasFocus, drawImages,
            isViewed(item), getScore(item)));
    }

    ////////////////////////////////////////////////////////////////////////////
    //                                                          Helper Functions
    ////////////////////////////////////////////////////////////////////////////
    private boolean isViewed(Item item) {
        if (item.getProperty("viewed") != null) {
            return (true);
        } else {
            return (false);
        }
    }
    
    private int getScore(Item item) {
        String score = item.getProperty("score");

        if (score != null) {
            return ((new Integer(score)).intValue());
        } else {
            return (50);
        }
    }
}
