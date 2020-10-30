MODULE = LedPanel
BUILD_DIR = target/bitstream
CONSTRAINTS = ../../src/main/resources/constraints.pcf

all: build

build: elaborate bitstream

elaborate:
	sbt --supershell=never "runMain ice.$(MODULE)"

bitstream:
	cd $(BUILD_DIR) && \
	yosys -q -p 'synth_ice40 -top $(MODULE) -blif $(MODULE).blif' $(MODULE).v && \
	arachne-pnr -d 5k -o $(MODULE).asc -p $(CONSTRAINTS) $(MODULE).blif && \
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
