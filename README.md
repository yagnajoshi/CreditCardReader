# CreditCardReader

[![](https://jitpack.io/v/yagnajoshi/CreditCardReader.svg)](https://jitpack.io/#yagnajoshi/CreditCardReader)


<H3><B>How to</B></H3>

To get a Git project into your build:

<B>Step 1.</B> 
Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories:

allprojects
  {

    repositories 
    {
   ...
    maven { 
  
        url 'https://jitpack.io'
  
         }
  
      }  
}

<B>Step 2. </B> 
Add the dependency

dependencies {

implementation 'com.github.yagnajoshi:CreditCardReader:Tag'

}
