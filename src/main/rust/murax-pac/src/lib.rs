#![no_std]

use riscv as arch;

pub mod register;
pub use crate::register::{RORegister, UnsafeRORegister};
pub use crate::register::{RWRegister, UnsafeRWRegister};
pub use crate::register::{UnsafeWORegister, WORegister};

mod murax;
pub use murax::*;
