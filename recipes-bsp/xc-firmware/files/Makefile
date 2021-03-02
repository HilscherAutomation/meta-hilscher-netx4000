objs = $(shell ls rpec*.c tpec*.c rpu*.c tpu*.c | sed 's/\.c/\.bin/')

all: $(objs)

%.bin: %.o
	$(OBJCOPY) -O binary --only-section .rodata.XcCode* $< $@

%.o: %.c
	$(CC) $(CFLAGS) -c $<

clean:
	$(RM) *.o