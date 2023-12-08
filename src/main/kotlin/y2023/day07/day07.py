#!/bin/python3

from typing import Tuple

input = ["input_test.txt", "input_prod.txt"][1]
lines = open(input, 'r').read().strip().split("\n")

def mmap(line: str)-> Tuple[str, int]:
    sp = line.split(" ")
    key = sp[0]
    score = sp[-1]
    symb = list("AKQJT98765432")
    to   = list("abcdefghijklm")
    for pair in zip(symb, to):
        key = key.replace(pair[0], pair[1])

    ta = sorted(key)
    prefix = None
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


    key = str(prefix) + key
    return (key, int(score))

items = list(reversed(sorted(map(mmap, lines))))
# for item in items:
#     print(item[0], item[1])

sum = 0
for rank in range(1, len(items) + 1):
    score = items[rank - 1][1]
    print(f"{sum:016d} {rank:016d} {score}")
    sum += score * rank

print(sum)
