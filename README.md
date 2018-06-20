# Ruler[![Release](https://jitpack.io/v/TangKe/Ruler.svg)](https://jitpack.io/#TangKe/Ruler)
[查看中文介绍](https://raw.githubusercontent.com/TangKe/Ruler/master/README.zh-cn.md)

[![Click to view demo video](https://raw.githubusercontent.com/TangKe/Ruler/master/resources/snapshot.png)](https://v.youku.com/v_show/id_XMzY3NTIxNzAxNg==.html?spm=a2h3j.8428770.3416059.1)

Provide a horizontal scrollable ruler widget, developer can get the value which user scrolled to
The widget is drawn by canvas. All elements is customizable
Provide a style by default

Customizable elements:

- distance between two scale
- scale color
- ruler color(underline)
- scale group count
- indicator(a drawable alway show in center)
- scale height
- scale widget
- ruler height
- label text color
- label text size
- format of the label

## Install

1. Add following lines in your project root `build.gradle` file

   ```groovy
   allprojects {
       repositories {
           maven { uri 'https://jitpack.io' }
       }
   }
   ```

2. Add following lines in your app `build.gradle` file

   ```groovy
   dependencies {
     implementation 'com.github.TangKe:Ruler:1.0.0'
   }
   ```

## How to use

1. In xml layout

   ```xml
   <?xml version="1.0" encoding="utf-8"?>
   <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
       android:layout_width="match_parent"
       android:layout_height="match_parent"
       android:orientation="vertical">
   
       <me.tangke.ruler.RulerView
           android:layout_width="match_parent"
           android:layout_height="wrap_content" />
   
   </LinearLayout>
   ```

2. Or use `RulerView` as normal view in your `Java` code, About the api please view the Javadoc for detail.

## Attributes

| Attribute Name | Attribute Type | Description         |
| ------------------- | ---------------- | ----------------------- |
| stepWidth           | dimension        | Distance between scales |
| rulerValueFormatter | string           | Ruler value formter class full name, must a subclass of  `RulerValueFormatter` |
| scaleColor          | color\|reference | Scale color |
| rulerColor          | color\|reference | Ruler color(underline) |
| sectionScaleCount   | integer          | Scale group count |
| scaleMinHeight      | dimension        | Secondary scale height |
| scaleMaxHeight      | dimension        | Primary scale height |
| scaleSize           | dimension        | Scale width |
| rulerSize           | dimension        | Ruler height(underline) |
| indicator           | reference        | Indicator(always displayed in center) |
| maxValue            | integer          | Value upper limit(0-10000) |
| minValue            | integer          | Value lower limit(0-10000) |
| value               | integer          | Current value(0-10000) |
| android:textSize    | dimension        | Label text size |
| android:textColor   | color\|reference | Label text color |

## License

```
    Copyright 2018 TangKe

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
```

   ​

   