MODULE = TTA
BUILD_DIR = target/bitstream
CONSTRAINTS = ../../src/main/resources/constraints.pcf

all: build

build: elaborate bitstream

elaborate:
	sbt --supershell=never "runMain ice.$(MODULE)"

bitstream:
	cd $(BUILD_DIR) && \
	yosys -q -p 'synth_ice40 -top $(MODULE) -json $(MODULE).json' $(MODULE).v && \
	nextpnr-ice40 --up5k --json $(MODULE).json --pcf $(CONSTRAINTS) --asc $(MODULE).asc && \
	icetime -d up5k -mtr $(MODULE).rpt $(MODULE).asc && \
	icepack $(MODULE).asc $(MODULE).bin

prog:
	iceprog -S $(BUILD_DIR)/$(MODULE).bin

flash:
	iceprog -p $(BUILD_DIR)/$(MODULE).bin

clean:
	sbt clean --supershell=never
	rm -rf $(BUILD_DIR)

.SECONDARY:
.PHONY: all bitstream build clean elaborate flash prog
