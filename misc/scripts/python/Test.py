"""\
A test of the scripting UI and HotSheet.
"""

import java
from java.awt import *
from javax.swing import *
import time

def exit(e): java.lang.System.exit(0)

def getField (f):
	t = f.getText ()
	if t == '':
		return 0
	else:
		return java.lang.Integer.parseInt (t)

def doMath (e):
	n1 = getField (f1)
	n2 = getField (f2)
	sum.setText (repr (n1 + n2))
	diff.setText (repr (n1 - n2))
	prod.setText (repr (n1 * n2))
	quo.setText (repr (n1 / n2))

print "itemStore size %d" % itemStore.size()
print "Current Time %s" % time.asctime()

frame = JFrame('Item Store Statistics', visible=1)
layout = GridLayout(3,1)
frame.contentPane.setLayout(layout)
label = JLabel("Current Time: %s" % time.asctime())
label1 = JLabel("Item Store Size: %d" % itemStore.size())
button = JButton("OK", actionPerformed=action)
frame.contentPane.add(label)
frame.contentPane.add(label1)
frame.contentPane.add(button)
frame.pack()

def action(event) :
    frame.dispose()

