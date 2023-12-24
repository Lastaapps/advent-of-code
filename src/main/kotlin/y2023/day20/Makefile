GCC = g++
CFLAGS = -Wall -pedantic -g3 -fsanitize=address -ftrapv -Wstrict-overflow
# CFLAGS = -Wall -pedantic -Ofast
TARGET = day20

.PHONY: run build
.DEFAULT_GOAL := run

build: $(TARGET)

run: $(TARGET) Makefile
	@ time ./$(TARGET)

$(TARGET): $(TARGET).cpp
	$(GCC) $(CFLAGS) -o $@ $^
