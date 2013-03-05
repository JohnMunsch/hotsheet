"""\
A test of the scripting UI and HotSheet.
"""

import sys
import java
from java.awt import *
from javax.swing import *

def action(event) :
    frame.dispose()

frame = JFrame('Item Store Statistics', visible=1)
layout = GridLayout(len(sys.path) + 2,1)
frame.contentPane.setLayout(layout)

for x in sys.path:
    label = JLabel("sys.path: %s" % x)
    frame.contentPane.add(label)

label1 = JLabel("Item Store Size: %d" % itemStore.size())
button = JButton("OK", actionPerformed=action)

frame.contentPane.add(label1)
frame.contentPane.add(button)
frame.pack()
