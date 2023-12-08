#!/bin/python3

from typing import Tuple

input = ["input_test.txt", "input_prod.txt"][1]
lines = open(input, 'r').read().strip().split("\n")

is_part_01: bool

def mmap(line: str)-> Tuple[str, int]:
    sp = line.split(" ")
    key = sp[0]
    score = sp[-1]

    symb = list("AKQJT98765432")

    if is_part_01:
        j_cnt = 0
        to   = list("abcdefghijklm")
    else:
        j_cnt = key.count("J")
        to   = list("abcxefghijklm")

    for pair in zip(symb, to):
        key = key.replace(pair[0], pair[1])

    ta = key
    if not is_part_01:
        ta = ta.replace("x", "p", 1)
        ta = ta.replace("x", "q", 1)
        ta = ta.replace("x", "r", 1)
        ta = ta.replace("x", "s", 1)
        ta = ta.replace("x", "t", 1)

    ta = sorted(ta)
    prefix = None
    if j_cnt == 0:
        # 5 same
        if   ta[0] == ta[1] and ta[1] == ta[2] and ta[2] == ta[3] and ta[3] == ta[4]:
            prefix = 0

        # 4 same
        elif ta[0] == ta[1] and ta[1] == ta[2] and ta[2] == ta[3]                   :
            prefix = 1
        elif                    ta[1] == ta[2] and ta[2] == ta[3] and ta[3] == ta[4]:
            prefix = 1

        # Full house
        elif   ta[0] == ta[1] and ta[1] == ta[2]                    and ta[3] == ta[4]:
            prefix = 2
        elif   ta[0] == ta[1]                    and ta[2] == ta[3] and ta[3] == ta[4]:
            prefix = 2

        # 3 same
        elif ta[0] == ta[1] and ta[1] == ta[2]                                      :
            prefix = 3
        elif                    ta[1] == ta[2] and ta[2] == ta[3]                   :
            prefix = 3
        elif                                       ta[2] == ta[3] and ta[3] == ta[4]:
            prefix = 3

        # 2 pairs
        elif ta[0] == ta[1]                    and ta[2] == ta[3]                   :
            prefix = 4
        elif ta[0] == ta[1]                                       and ta[3] == ta[4]:
            prefix = 4
        elif                    ta[1] == ta[2]                    and ta[3] == ta[4]:
            prefix = 4

        # 2 same
        elif ta[0] == ta[1]                                                                            :
            prefix = 5
        elif                    ta[1] == ta[2]                                                         :
            prefix = 5
        elif                                       ta[2] == ta[3]                                      :
            prefix = 5
        elif                                                          ta[3] == ta[4]                   :
            prefix = 5

        # 0 same
        else:
            prefix = 6
    elif j_cnt == 1:
        # 4 same + J -> 5 same
        if   ta[0] == ta[1] and ta[1] == ta[2] and ta[2] == ta[3]                   :
            prefix = 0
        elif                    ta[1] == ta[2] and ta[2] == ta[3] and ta[3] == ta[4]:
            prefix = 0

        # 3 same + J -> 4 same
        elif ta[0] == ta[1] and ta[1] == ta[2]                                      :
            prefix = 1
        elif                    ta[1] == ta[2] and ta[2] == ta[3]                   :
            prefix = 1
        elif                                       ta[2] == ta[3] and ta[3] == ta[4]:
            prefix = 1

        # 2 pairs + J -> Full house
        elif ta[0] == ta[1]                    and ta[2] == ta[3]                   :
            prefix = 2
        elif ta[0] == ta[1]                                       and ta[3] == ta[4]:
            prefix = 2
        elif                    ta[1] == ta[2]                    and ta[3] == ta[4]:
            prefix = 2

        # 2 same + J -> 3 same
        elif ta[0] == ta[1]                                                                            :
            prefix = 3
        elif                    ta[1] == ta[2]                                                         :
            prefix = 3
        elif                                       ta[2] == ta[3]                                      :
            prefix = 3
        elif                                                          ta[3] == ta[4]                   :
            prefix = 3

        # 0 same + J -> 2 same
        else:
            prefix = 5
    elif j_cnt == 2:
        # 3 same + JJ -> 5 same
        if   ta[0] == ta[1] and ta[1] == ta[2]                                      :
            prefix = 0
        elif                    ta[1] == ta[2] and ta[2] == ta[3]                   :
            prefix = 0
        elif                                       ta[2] == ta[3] and ta[3] == ta[4]:
            prefix = 0

        # 2 same + JJ -> 4 same
        elif ta[0] == ta[1]                                                                            :
            prefix = 1
        elif                    ta[1] == ta[2]                                                         :
            prefix = 1
        elif                                       ta[2] == ta[3]                                      :
            prefix = 1
        elif                                                          ta[3] == ta[4]                   :
            prefix = 1

        # 0 same + JJ -> 3 same
        else:
            prefix = 3
    elif j_cnt == 3:
        # 2 same + JJJ -> 5 same
        if   ta[0] == ta[1]                                                                            :
            prefix = 0
        elif                    ta[1] == ta[2]                                                         :
            prefix = 0
        elif                                       ta[2] == ta[3]                                      :
            prefix = 0
        elif                                                          ta[3] == ta[4]                   :
            prefix = 0

        # 0 same + JJJ -> 4 same
        else:
            prefix = 1
    elif j_cnt == 4:
        # 0 same + JJJJ -> 5 same
        prefix = 0
    elif j_cnt == 5:
        # 0 same + JJJJ -> 5 same
        prefix = 0

    assert(prefix != None)
    key = str(prefix) + key
    return (key, int(score))

is_part_01 = True
is_part_01 = False
items = list(reversed(sorted(map(mmap, lines))))
# for item in items:
#     print(item[0], item[1])

sum = 0
for rank in range(1, len(items) + 1):
    score = items[rank - 1][1]
    print(f"{sum:016d} {rank:016d} {score}")
    sum += score * rank

print(sum)
