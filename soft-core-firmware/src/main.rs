#![no_std]
#![no_main]

extern crate panic_halt;

use core::ptr::write_volatile;
use riscv_rt::entry;

const GPIO_OUTPUT_ENABLE: u32 = 0xf0000008;
const GPIO_OUTPUT: u32 = 0xf0000004;

#[entry]
unsafe fn main() -> ! {
    write_volatile(GPIO_OUTPUT_ENABLE as *mut u32, 0xff);

    let mut cnt = 0;
    loop {
        cnt += 1;
        write_volatile(GPIO_OUTPUT as *mut u32, cnt >> 16);
    }
}
