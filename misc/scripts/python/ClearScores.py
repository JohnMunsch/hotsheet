"""\
If you've totally messed up the scores on all your news items while trying out
scripts you can restore everything back to the default score of 50.
"""

import java

iterator = itemStore.iterator()

while iterator.hasNext() :
    item = iterator.next()
    item.setProperty("score", str("50"))

    