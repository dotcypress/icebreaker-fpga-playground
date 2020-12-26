#![doc = "Peripheral access API for MURAX SOC microcontrollers (generated using svd2rust v0.17.0)\n\nYou can find an overview of the API [here].\n\n[here]: https://docs.rs/svd2rust/0.17.0/svd2rust/#peripheral-api"]
#![deny(const_err)]
#![deny(dead_code)]
#![deny(improper_ctypes)]
#![deny(no_mangle_generic_items)]
#![deny(non_shorthand_field_patterns)]
#![deny(overflowing_literals)]
#![deny(path_statements)]
#![deny(patterns_in_fns_without_body)]
#![deny(private_in_public)]
#![deny(unconditional_recursion)]
#![deny(unused_allocation)]
#![deny(unused_parens)]
#![deny(while_true)]
#![allow(non_camel_case_types)]
#![allow(non_snake_case)]
#![no_std]
extern crate bare_metal;
extern crate riscv;
#[cfg(feature = "rt")]
extern crate riscv_rt;
extern crate vcell;
use core::marker::PhantomData;
use core::ops::Deref;
#[doc = r"Number available in the NVIC for configuring priority"]
pub const NVIC_PRIO_BITS: u8 = 4;
#[allow(unused_imports)]
use generic::*;
#[doc = r"Common register and bit access and modify traits"]
pub mod generic {
  use core::marker;
  #[doc = "This trait shows that register has `read` method"]
  #[doc = ""]
  #[doc = "Registers marked with `Writable` can be also `modify`'ed"]
  pub trait Readable {}
  #[doc = "This trait shows that register has `write`, `write_with_zero` and `reset` method"]
  #[doc = ""]
  #[doc = "Registers marked with `Readable` can be also `modify`'ed"]
  pub trait Writable {}
  #[doc = "Reset value of the register"]
  #[doc = ""]
  #[doc = "This value is initial value for `write` method."]
  #[doc = "It can be also directly writed to register by `reset` method."]
  pub trait ResetValue {
    #[doc = "Register size"]
    type Type;
    #[doc = "Reset value of the register"]
    fn reset_value() -> Self::Type;
  }
  #[doc = "This structure provides volatile access to register"]
  pub struct Reg<U, REG> {
    register: vcell::VolatileCell<U>,
    _marker: marker::PhantomData<REG>,
  }
  unsafe impl<U: Send, REG> Send for Reg<U, REG> {}
  impl<U, REG> Reg<U, REG>
  where
    Self: Readable,
    U: Copy,
  {
    #[doc = "Reads the contents of `Readable` register"]
    #[doc = ""]
    #[doc = "You can read the contents of a register in such way:"]
    #[doc = "```ignore"]
    #[doc = "let bits = periph.reg.read().bits();"]
    #[doc = "```"]
    #[doc = "or get the content of a particular field of a register."]
    #[doc = "```ignore"]
    #[doc = "let reader = periph.reg.read();"]
    #[doc = "let bits = reader.field1().bits();"]
    #[doc = "let flag = reader.field2().bit_is_set();"]
    #[doc = "```"]
    #[inline(always)]
    pub fn read(&self) -> R<U, Self> {
      R {
        bits: self.register.get(),
        _reg: marker::PhantomData,
      }
    }
  }
  impl<U, REG> Reg<U, REG>
  where
    Self: ResetValue<Type = U> + Writable,
    U: Copy,
  {
    #[doc = "Writes the reset value to `Writable` register"]
    #[doc = ""]
    #[doc = "Resets the register to its initial state"]
    #[inline(always)]
    pub fn reset(&self) {
      self.register.set(Self::reset_value())
    }
  }
  impl<U, REG> Reg<U, REG>
  where
    Self: ResetValue<Type = U> + Writable,
    U: Copy,
  {
    #[doc = "Writes bits to `Writable` register"]
    #[doc = ""]
    #[doc = "You can write raw bits into a register:"]
    #[doc = "```ignore"]
    #[doc = "periph.reg.write(|w| unsafe { w.bits(rawbits) });"]
    #[doc = "```"]
    #[doc = "or write only the fields you need:"]
    #[doc = "```ignore"]
    #[doc = "periph.reg.write(|w| w"]
    #[doc = "    .field1().bits(newfield1bits)"]
    #[doc = "    .field2().set_bit()"]
    #[doc = "    .field3().variant(VARIANT)"]
    #[doc = ");"]
    #[doc = "```"]
    #[doc = "Other fields will have reset value."]
    #[inline(always)]
    pub fn write<F>(&self, f: F)
    where
      F: FnOnce(&mut W<U, Self>) -> &mut W<U, Self>,
    {
      self.register.set(
        f(&mut W {
          bits: Self::reset_value(),
          _reg: marker::PhantomData,
        })
        .bits,
      );
    }
  }
  impl<U, REG> Reg<U, REG>
  where
    Self: Writable,
    U: Copy + Default,
  {
    #[doc = "Writes Zero to `Writable` register"]
    #[doc = ""]
    #[doc = "Similar to `write`, but unused bits will contain 0."]
    #[inline(always)]
    pub fn write_with_zero<F>(&self, f: F)
    where
      F: FnOnce(&mut W<U, Self>) -> &mut W<U, Self>,
    {
      self.register.set(
        f(&mut W {
          bits: U::default(),
          _reg: marker::PhantomData,
        })
        .bits,
      );
    }
  }
  impl<U, REG> Reg<U, REG>
  where
    Self: Readable + Writable,
    U: Copy,
  {
    #[doc = "Modifies the contents of the register"]
    #[doc = ""]
    #[doc = "E.g. to do a read-modify-write sequence to change parts of a register:"]
    #[doc = "```ignore"]
    #[doc = "periph.reg.modify(|r, w| unsafe { w.bits("]
    #[doc = "   r.bits() | 3"]
    #[doc = ") });"]
    #[doc = "```"]
    #[doc = "or"]
    #[doc = "```ignore"]
    #[doc = "periph.reg.modify(|_, w| w"]
    #[doc = "    .field1().bits(newfield1bits)"]
    #[doc = "    .field2().set_bit()"]
    #[doc = "    .field3().variant(VARIANT)"]
    #[doc = ");"]
    #[doc = "```"]
    #[doc = "Other fields will have value they had before call `modify`."]
    #[inline(always)]
    pub fn modify<F>(&self, f: F)
    where
      for<'w> F: FnOnce(&R<U, Self>, &'w mut W<U, Self>) -> &'w mut W<U, Self>,
    {
      let bits = self.register.get();
      self.register.set(
        f(
          &R {
            bits,
            _reg: marker::PhantomData,
          },
          &mut W {
            bits,
            _reg: marker::PhantomData,
          },
        )
        .bits,
      );
    }
  }
  #[doc = "Register/field reader"]
  #[doc = ""]
  #[doc = "Result of the [`read`](Reg::read) method of a register."]
  #[doc = "Also it can be used in the [`modify`](Reg::read) method"]
  pub struct R<U, T> {
    pub(crate) bits: U,
    _reg: marker::PhantomData<T>,
  }
  impl<U, T> R<U, T>
  where
    U: Copy,
  {
    #[doc = "Create new instance of reader"]
    #[inline(always)]
    pub(crate) fn new(bits: U) -> Self {
      Self {
        bits,
        _reg: marker::PhantomData,
      }
    }
    #[doc = "Read raw bits from register/field"]
    #[inline(always)]
    pub fn bits(&self) -> U {
      self.bits
    }
  }
  impl<U, T, FI> PartialEq<FI> for R<U, T>
  where
    U: PartialEq,
    FI: Copy + Into<U>,
  {
    #[inline(always)]
    fn eq(&self, other: &FI) -> bool {
      self.bits.eq(&(*other).into())
    }
  }
  impl<FI> R<bool, FI> {
    #[doc = "Value of the field as raw bits"]
    #[inline(always)]
    pub fn bit(&self) -> bool {
      self.bits
    }
    #[doc = "Returns `true` if the bit is clear (0)"]
    #[inline(always)]
    pub fn bit_is_clear(&self) -> bool {
      !self.bit()
    }
    #[doc = "Returns `true` if the bit is set (1)"]
    #[inline(always)]
    pub fn bit_is_set(&self) -> bool {
      self.bit()
    }
  }
  #[doc = "Register writer"]
  #[doc = ""]
  #[doc = "Used as an argument to the closures in the [`write`](Reg::write) and [`modify`](Reg::modify) methods of the register"]
  pub struct W<U, REG> {
    #[doc = "Writable bits"]
    pub(crate) bits: U,
    _reg: marker::PhantomData<REG>,
  }
  impl<U, REG> W<U, REG> {
    #[doc = "Writes raw bits to the register"]
    #[inline(always)]
    pub unsafe fn bits(&mut self, bits: U) -> &mut Self {
      self.bits = bits;
      self
    }
  }
  #[doc = "Used if enumerated values cover not the whole range"]
  #[derive(Clone, Copy, PartialEq)]
  pub enum Variant<U, T> {
    #[doc = "Expected variant"]
    Val(T),
    #[doc = "Raw bits"]
    Res(U),
  }
}
#[doc = "GPIO"]
pub struct GPIO {
  _marker: PhantomData<*const ()>,
}
unsafe impl Send for GPIO {}
impl GPIO {
  #[doc = r"Returns a pointer to the register block"]
  #[inline(always)]
  pub const fn ptr() -> *const gpio::RegisterBlock {
    0xf000_0000 as *const _
  }
}
impl Deref for GPIO {
  type Target = gpio::RegisterBlock;
  #[inline(always)]
  fn deref(&self) -> &Self::Target {
    unsafe { &*GPIO::ptr() }
  }
}
#[doc = "GPIO"]
pub mod gpio {
  #[doc = r"Register block"]
  #[repr(C)]
  pub struct RegisterBlock {
    #[doc = "0x00 - "]
    pub input: INPUT,
    #[doc = "0x04 - "]
    pub output: OUTPUT,
    #[doc = "0x08 - "]
    pub output_enable: OUTPUT_ENABLE,
  }
  #[doc = "\n\nThis register you can [`read`](crate::generic::Reg::read), [`reset`](crate::generic::Reg::reset), [`write`](crate::generic::Reg::write), [`write_with_zero`](crate::generic::Reg::write_with_zero), [`modify`](crate::generic::Reg::modify). See [API](https://docs.rs/svd2rust/#read--modify--write-api).\n\nFor information about available fields see [input](input) module"]
  pub type INPUT = crate::Reg<u32, _INPUT>;
  #[allow(missing_docs)]
  #[doc(hidden)]
  pub struct _INPUT;
  #[doc = "`read()` method returns [input::R](input::R) reader structure"]
  impl crate::Readable for INPUT {}
  #[doc = "`write(|w| ..)` method takes [input::W](input::W) writer structure"]
  impl crate::Writable for INPUT {}
  #[doc = ""]
  pub mod input {
    #[doc = "Reader of register INPUT"]
    pub type R = crate::R<u32, super::INPUT>;
    #[doc = "Writer for register INPUT"]
    pub type W = crate::W<u32, super::INPUT>;
    #[doc = "Register INPUT `reset()`'s with value 0"]
    impl crate::ResetValue for super::INPUT {
      type Type = u32;
      #[inline(always)]
      fn reset_value() -> Self::Type {
        0
      }
    }
    #[doc = "Reader of field `input`"]
    pub type INPUT_R = crate::R<u32, u32>;
    #[doc = "Write proxy for field `input`"]
    pub struct INPUT_W<'a> {
      w: &'a mut W,
    }
    impl<'a> INPUT_W<'a> {
      #[doc = r"Writes raw bits to the field"]
      #[inline(always)]
      pub unsafe fn bits(self, value: u32) -> &'a mut W {
        self.w.bits = (self.w.bits & !0xffff_ffff) | ((value as u32) & 0xffff_ffff);
        self.w
      }
    }
    impl R {
      #[doc = "Bits 0:31"]
      #[inline(always)]
      pub fn input(&self) -> INPUT_R {
        INPUT_R::new((self.bits & 0xffff_ffff) as u32)
      }
    }
    impl W {
      #[doc = "Bits 0:31"]
      #[inline(always)]
      pub fn input(&mut self) -> INPUT_W {
        INPUT_W { w: self }
      }
    }
  }
  #[doc = "\n\nThis register you can [`read`](crate::generic::Reg::read), [`reset`](crate::generic::Reg::reset), [`write`](crate::generic::Reg::write), [`write_with_zero`](crate::generic::Reg::write_with_zero), [`modify`](crate::generic::Reg::modify). See [API](https://docs.rs/svd2rust/#read--modify--write-api).\n\nFor information about available fields see [output](output) module"]
  pub type OUTPUT = crate::Reg<u32, _OUTPUT>;
  #[allow(missing_docs)]
  #[doc(hidden)]
  pub struct _OUTPUT;
  #[doc = "`read()` method returns [output::R](output::R) reader structure"]
  impl crate::Readable for OUTPUT {}
  #[doc = "`write(|w| ..)` method takes [output::W](output::W) writer structure"]
  impl crate::Writable for OUTPUT {}
  #[doc = ""]
  pub mod output {
    #[doc = "Reader of register OUTPUT"]
    pub type R = crate::R<u32, super::OUTPUT>;
    #[doc = "Writer for register OUTPUT"]
    pub type W = crate::W<u32, super::OUTPUT>;
    #[doc = "Register OUTPUT `reset()`'s with value 0"]
    impl crate::ResetValue for super::OUTPUT {
      type Type = u32;
      #[inline(always)]
      fn reset_value() -> Self::Type {
        0
      }
    }
    #[doc = "Reader of field `output`"]
    pub type OUTPUT_R = crate::R<u32, u32>;
    #[doc = "Write proxy for field `output`"]
    pub struct OUTPUT_W<'a> {
      w: &'a mut W,
    }
    impl<'a> OUTPUT_W<'a> {
      #[doc = r"Writes raw bits to the field"]
      #[inline(always)]
      pub unsafe fn bits(self, value: u32) -> &'a mut W {
        self.w.bits = (self.w.bits & !0xffff_ffff) | ((value as u32) & 0xffff_ffff);
        self.w
      }
    }
    impl R {
      #[doc = "Bits 0:31"]
      #[inline(always)]
      pub fn output(&self) -> OUTPUT_R {
        OUTPUT_R::new((self.bits & 0xffff_ffff) as u32)
      }
    }
    impl W {
      #[doc = "Bits 0:31"]
      #[inline(always)]
      pub fn output(&mut self) -> OUTPUT_W {
        OUTPUT_W { w: self }
      }
    }
  }
  #[doc = "\n\nThis register you can [`read`](crate::generic::Reg::read), [`reset`](crate::generic::Reg::reset), [`write`](crate::generic::Reg::write), [`write_with_zero`](crate::generic::Reg::write_with_zero), [`modify`](crate::generic::Reg::modify). See [API](https://docs.rs/svd2rust/#read--modify--write-api).\n\nFor information about available fields see [output_enable](output_enable) module"]
  pub type OUTPUT_ENABLE = crate::Reg<u32, _OUTPUT_ENABLE>;
  #[allow(missing_docs)]
  #[doc(hidden)]
  pub struct _OUTPUT_ENABLE;
  #[doc = "`read()` method returns [output_enable::R](output_enable::R) reader structure"]
  impl crate::Readable for OUTPUT_ENABLE {}
  #[doc = "`write(|w| ..)` method takes [output_enable::W](output_enable::W) writer structure"]
  impl crate::Writable for OUTPUT_ENABLE {}
  #[doc = ""]
  pub mod output_enable {
    #[doc = "Reader of register OUTPUT_ENABLE"]
    pub type R = crate::R<u32, super::OUTPUT_ENABLE>;
    #[doc = "Writer for register OUTPUT_ENABLE"]
    pub type W = crate::W<u32, super::OUTPUT_ENABLE>;
    #[doc = "Register OUTPUT_ENABLE `reset()`'s with value 0"]
    impl crate::ResetValue for super::OUTPUT_ENABLE {
      type Type = u32;
      #[inline(always)]
      fn reset_value() -> Self::Type {
        0
      }
    }
    #[doc = "Reader of field `output_enable`"]
    pub type OUTPUT_ENABLE_R = crate::R<u32, u32>;
    #[doc = "Write proxy for field `output_enable`"]
    pub struct OUTPUT_ENABLE_W<'a> {
      w: &'a mut W,
    }
    impl<'a> OUTPUT_ENABLE_W<'a> {
      #[doc = r"Writes raw bits to the field"]
      #[inline(always)]
      pub unsafe fn bits(self, value: u32) -> &'a mut W {
        self.w.bits = (self.w.bits & !0xffff_ffff) | ((value as u32) & 0xffff_ffff);
        self.w
      }
    }
    impl R {
      #[doc = "Bits 0:31"]
      #[inline(always)]
      pub fn output_enable(&self) -> OUTPUT_ENABLE_R {
        OUTPUT_ENABLE_R::new((self.bits & 0xffff_ffff) as u32)
      }
    }
    impl W {
      #[doc = "Bits 0:31"]
      #[inline(always)]
      pub fn output_enable(&mut self) -> OUTPUT_ENABLE_W {
        OUTPUT_ENABLE_W { w: self }
      }
    }
  }
}
#[no_mangle]
static mut DEVICE_PERIPHERALS: bool = false;
#[doc = r"All the peripherals"]
#[allow(non_snake_case)]
pub struct Peripherals {
  #[doc = "GPIO"]
  pub GPIO: GPIO,
}
impl Peripherals {
  #[doc = r"Returns all the peripherals *once*"]
  #[inline]
  pub fn take() -> Option<Self> {
    riscv::interrupt::free(|_| {
      if unsafe { DEVICE_PERIPHERALS } {
        None
      } else {
        Some(unsafe { Peripherals::steal() })
      }
    })
  }
  #[doc = r"Unchecked version of `Peripherals::take`"]
  #[inline]
  pub unsafe fn steal() -> Self {
    DEVICE_PERIPHERALS = true;
    Peripherals {
      GPIO: GPIO {
        _marker: PhantomData,
      },
    }
  }
}
