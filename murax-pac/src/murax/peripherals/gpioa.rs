#![allow(non_snake_case, non_upper_case_globals)]
#![allow(non_camel_case_types)]
//! General Purpose I/O

use crate::{RWRegister};
use core::marker::PhantomData;

/// Digital input
pub mod INPUT {
    pub mod input {
        /// Offset (0 bits)
        pub const offset: u32 = 0;
    
        /// Mask (32 bit: 0xffffffff << 0)
        pub const mask: u32 = 0xffffffff << offset;
    
        /// Read-only values (empty)
        pub mod R {}
        /// Write-only values (empty)
        pub mod W {}
        /// Read-write values (empty)
        pub mod RW {}
    
    }}

/// Digital output
pub mod OUTPUT {
}

/// I/O direction
pub mod DIRECTION {
}

pub struct RegisterBlock {
    /// Digital input
    pub INPUT: RWRegister<u32>,

    /// Digital output
    pub OUTPUT: RWRegister<u32>,

    /// I/O direction
    pub DIRECTION: RWRegister<u32>,
}

pub struct ResetValues {
    pub INPUT: u32,
    pub OUTPUT: u32,
    pub DIRECTION: u32,
}

pub struct Instance {
    pub(crate) addr: u32,
    pub(crate) _marker: PhantomData<*const RegisterBlock>,
}

impl ::core::ops::Deref for Instance {
    type Target = RegisterBlock;
    #[inline(always)]
    fn deref(&self) -> &RegisterBlock {
        unsafe { &*(self.addr as *const _) }
    }
}
