GCC = g++
CFLAGS += -std=c++20
CFLAGS += -Wall -pedantic -g3 -fsanitize=address -ftrapv -Wstrict-overflow
# CFLAGS += -Wall -pedantic -Ofast
TARGET = day25

.PHONY: run build
.DEFAULT_GOAL := run

build: $(TARGET)

run: $(TARGET) Makefile
	@ time ./$(TARGET)

$(TARGET): $(TARGET).cpp
	$(GCC) $(CFLAGS) -o $@ $^
