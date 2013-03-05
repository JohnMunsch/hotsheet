"""\
If the item is older than a set number of days it should be deleted. If I
haven't read it yet, I'm not going to.
"""

import java

iterator = itemStore.iterator()

while iterator.hasNext() :
    item = iterator.next()

    if item.getRetrieved()  :
        iterator.remove()
