# crate [![Build Status](https://travis-ci.org/apringle/crate.svg?branch=master)](https://travis-ci.org/apringle/crate)
Crate is a simple library for storing Android objects to disk. It provides a modest but powerful API for item management and stops you from writing tedious storage code.

## Usage
Crate can store any item that is compatible with Gson and implements the HasId interface :

```java
public class Cake implements HasId {

    private String id, icingColor;
    
    public Cake() { /* default constructor required by Gson*/ }
    public Cake(String id, String icingColor) { /*...*/ }
    
    // properties with getters and setters will be stored
    @Override
    public String getId() { /*...*/ }
    public void setId(String id) { /*...*/ }
    
    public String getIcingColor() { /*...*/ }
    public void setIcingColor(String icingColor) { /*...*/ }
}
```

Create crates by extending the Crate class :

```java
public class CakeCrate extends Crate<Cake> {

    public CakeCrate(Context context) {
        super(context);
    }
}
```

Crates can be used to store and retrieve items :

```java
CakeCrate cakeCrate = new CakeCrate(getApplicationContext());

// Store one or a collection of items
cakeCrate.put(new Cake("123","red"));
Cake cakeFromStorage = cakeCrate.withId("123"); // {id:"123",icingColor:"red"}

cakeCrate.remove("123");
boolean redCakeExists = cakeCrate.exists("123"); // false

// Store one or a collection of items with a tag
cakeCrate.put(new Cake("789","green"),"BEST_CAKES");
List<Cake> bestCakes = cakeCrate.withTag("BEST_CAKES"); // [{id:"789",icingColor:"green"}]

// Replace tagged items
List<Cake> newBestCakes = ...;
cakeCrate.replace("BEST_CAKES",newBestCakes);

cakeCrate.removeAll();
List<Cake> allCakes = cakeCrate.all(); // []

cakeCrate.close();
```

#### Download   [ ![Download](https://api.bintray.com/packages/apringle/crate/crate/images/download.svg) ](https://bintray.com/apringle/crate/crate/_latestVersion)

crate is available in Jcenter, grab the latest jar or use Gradle.

#### Gradle

```groovy
compile 'uk.co.alexpringle:crate:0.1.7'
```

#### License

    Copyright 2015 Alex Pringle
    
    Licensed under the Apache License, Version 2.0 (the "License"); you may not use this 
    file except in compliance with the License. You may obtain a copy of the License at
    
    http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software distributed 
    under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS 
    OF ANY KIND, either express or implied. See the License for the specific language 
    governing permissions and limitations under the License.
