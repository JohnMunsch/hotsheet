"""\
If the target string is part of the channel title we bump the score on the
item for that channel up by a number of points.
"""

import java
import re

searchString = "CNET"
increase = 10

iterator = itemStore.iterator()

while iterator.hasNext() :
    item = iterator.next()
    score = item.getProperty("score", "50")

    if re.search(searchString, item.getChannel().getTitle()) :
        item.setProperty("score", str(int(score) + increase))
    else :
        item.setProperty("score", str(score))


    