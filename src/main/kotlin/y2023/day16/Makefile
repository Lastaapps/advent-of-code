GCC = g++
# GCC_OPTIONS = -Wall -pedantic -g3 -fsanitize=address
GCC_OPTIONS = -Wall -pedantic -Ofast

.PHONY: run day16
.DEFAULT_GOAL: run

run: day16 Makefile
	@ time ./day16

day16: day16.cpp
	$(GCC) $(GCC_OPTIONS) -o $@ $^
