#![no_std]
#![no_main]

extern crate panic_halt;

use murax_pac::Peripherals;
use riscv_rt::entry;

#[entry]
unsafe fn main() -> ! {
  let device = Peripherals::take().unwrap();
  device.GPIO.output_enable.write(|w| w.bits(0xff));
  let mut cnt = 0;
  loop {
    cnt += 1;
    device.GPIO.output.write(|w| w.bits(cnt >> 16));
  }
}
