[![license](https://img.shields.io/github/license/mashape/apistatus.svg)](https://github.com/aurasphere/revolver-compiler/blob/master/LICENSE)
[![Donate](https://img.shields.io/badge/Donate-PayPal-orange.svg)](https://www.paypal.com/donate/?cmd=_donations&business=8UK2BZP2K8NSS)

# Revolver

## Overview
A simple compile-time dependency injection framework. Revolver works with [standard javax.inject annotations](https://docs.oracle.com/javaee/7/api/javax/inject/package-summary.html).

## Usage

### Quickstart

To register a component under Revolver context, you use the javax.inject.Singleton annotation:

    @Singleton
    public class MyComponent {
        ...
    }
    
Component injection is performed by using the javax.inject.Inject annotation and invoking the Revolver.inject method by passing the object to inject (usually done in the constructor):


    public class MyMainClass {
   
        @Inject
        public MyComponent injectedComponent;
       
        public MyMainClass() {
            Revolver.inject(this);
        }
   
    }

The Revolver class is auto-generated during compilation, so you need to run a build (which will fail the first time for this reason) before you can use it. And that's it!

### Injection types
Revolver supports 3 types of injection: field injection, setter injection and constructor injection.

#### Field injection
To perform field injection, simply add @Inject to the field you want to inject:

       public class MyClass {
           
           @Inject
           public MyComponent injectedComponent;
           
           public MyClass() {
               Revolver.inject(this);
           }
       
       }

#### Setter injection
Setter injection will be automatically performed if the annotated field is not private. In this case, Revolver will look for a public setter named following the standard JavaBeans convention with only one argument of the same type of the field:

       public class MyClass {
           
           @Inject
           private MyComponent injectedComponent;
           
           public MyClass() {
               Revolver.inject(this);
           }
           
           // This method will be called.
           public void setInjectedComponent(MyComponent injectedComponent) {
               this.injectedComponent = injectedComponent;
           }
       
       }

#### Constructor injection
Constructor injection will be performed if any registered component specifies arguments in its constructor:

    @Singleton
    public class MyComponentOne {
  
    }
    
    @Singleton
    public class MyComponentTwo {
   
        public MyComponentOne myComponentOne;
       
        public MyComponentTwo(MyComponentOne myComponentOne) {
            this.myComponentOne = myComponentOne;
        }
   
    }
    
    public class MyMainClass {
   
        // This field will contain an injected component one!
        @Inject
        public MyComponentTwo myComponentTwo;
       
        public MyMainClass() {
            Revolver.inject(this);
        }
   
    }

If your component has more than one constructor, you can choose which one will be called by annotating it with @Inject:

    @Singleton
    public class MyComponentTwo {
   
        public MyComponentOne myComponentOne;
        
        public MyComponentTwo() {}
       
        // This is the constructor that will be called by Revolver
        @Inject
        public MyComponentTwo(MyComponentOne myComponentOne) {
            this.myComponentOne = myComponentOne;
        }
    }

### Programmatic Components
Sometimes, you need to register components from classes that are not under your control (for example, 3rd-party libraries). In this case, you can create a method that returns the component and annotate both the class and the method with @Singleton.
The method can have as many arguments as you want, and they will be automatically injected:

    @Singleton
    public class MyComponentProvider {
   
        // The parameter will be provided by Revolver if it's a registered component
        @Singleton
        public Retrofit createRetrofit(String myApiUrl) {
           return new Retrofit.Builder().baseUrl(myApiUrl).build();
        }
    }

Notice that like all other components managed by Revolver, anything produced by your method will be a Singleton, meaning that only one instance is produced. In this example, each time you require a Retrofit component for injection, the same component will be injected. The defined method will be thus called only once.

When you start creating components this way, it's easy to generate ambiguities in types. In this case, you give the produced component an identifier using the javax.inject.Named annotation:

    @Singleton
    public class MyComponentProvider {
   
        @Named("apiUrl")
        @Singleton
        public String createApiUrl() {
            return "https://github.com";
        }
        
        @Named("helloMessage")
        @Singleton
        public String createHelloWorldMessage() {
            return "Hello World";
        }
    }
    
    public class MainClass {
    
        @Named("helloMessage")
        public String msg;
   
        public MainClass() {
            Revolver.inject(this);
            // Prints Hello World
            System.out.println(msg);
        }
    }



