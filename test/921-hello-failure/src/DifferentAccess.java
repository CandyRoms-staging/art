/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.Base64;

class DifferentAccess {
  // The following is a base64 encoding of the following class.
  // public class NotTransform {
  //   public void sayHi(String name) {
  //     throw new Error("Should not be called!");
  //   }
  // }
  private static final byte[] CLASS_BYTES = Base64.getDecoder().decode(
    "yv66vgAAADQAFQoABgAPBwAQCAARCgACABIHABMHABQBAAY8aW5pdD4BAAMoKVYBAARDb2RlAQAP" +
    "TGluZU51bWJlclRhYmxlAQAFc2F5SGkBABUoTGphdmEvbGFuZy9TdHJpbmc7KVYBAApTb3VyY2VG" +
    "aWxlAQAOVHJhbnNmb3JtLmphdmEMAAcACAEAD2phdmEvbGFuZy9FcnJvcgEAFVNob3VsZCBub3Qg" +
    "YmUgY2FsbGVkIQwABwAMAQAJVHJhbnNmb3JtAQAQamF2YS9sYW5nL09iamVjdAAhAAUABgAAAAAA" +
    "AgABAAcACAABAAkAAAAdAAEAAQAAAAUqtwABsQAAAAEACgAAAAYAAQAAAAEAAQALAAwAAQAJAAAA" +
    "IgADAAIAAAAKuwACWRIDtwAEvwAAAAEACgAAAAYAAQAAAAMAAQANAAAAAgAO");
  private static final byte[] DEX_BYTES = Base64.getDecoder().decode(
    "ZGV4CjAzNQANVRT7zleRLG4E5DhtK7OtoDxZlUQMI5eQAgAAcAAAAHhWNBIAAAAAAAAAAPwBAAAL" +
    "AAAAcAAAAAUAAACcAAAAAgAAALAAAAAAAAAAAAAAAAQAAADIAAAAAQAAAOgAAACIAQAACAEAAEoB" +
    "AABSAQAAXwEAAHIBAACGAQAAmgEAALEBAADBAQAAxAEAAMgBAADcAQAAAQAAAAIAAAADAAAABAAA" +
    "AAcAAAAHAAAABAAAAAAAAAAIAAAABAAAAEQBAAAAAAAAAAAAAAAAAQAKAAAAAQABAAAAAAACAAAA" +
    "AAAAAAAAAAABAAAAAgAAAAAAAAAGAAAAAAAAAO4BAAAAAAAAAQABAAEAAADjAQAABAAAAHAQAwAA" +
    "AA4ABAACAAIAAADoAQAACQAAACIAAQAbAQUAAABwIAIAEAAnAAAAAQAAAAMABjxpbml0PgALTFRy" +
    "YW5zZm9ybTsAEUxqYXZhL2xhbmcvRXJyb3I7ABJMamF2YS9sYW5nL09iamVjdDsAEkxqYXZhL2xh" +
    "bmcvU3RyaW5nOwAVU2hvdWxkIG5vdCBiZSBjYWxsZWQhAA5UcmFuc2Zvcm0uamF2YQABVgACVkwA" +
    "EmVtaXR0ZXI6IGphY2stNC4yMAAFc2F5SGkAAQAHDgADAQAHDgAAAAEBAIGABIgCAQGgAgwAAAAA" +
    "AAAAAQAAAAAAAAABAAAACwAAAHAAAAACAAAABQAAAJwAAAADAAAAAgAAALAAAAAFAAAABAAAAMgA" +
    "AAAGAAAAAQAAAOgAAAABIAAAAgAAAAgBAAABEAAAAQAAAEQBAAACIAAACwAAAEoBAAADIAAAAgAA" +
    "AOMBAAAAIAAAAQAAAO4BAAAAEAAAAQAAAPwBAAA=");

  public static void doTest(Transform t) {
    t.sayHi("DifferentAccess");
    try {
      Main.doCommonClassRedefinition(Transform.class, CLASS_BYTES, DEX_BYTES);
    } catch (Exception e) {
      System.out.println(
          "Transformation error : " + e.getClass().getName() + "(" + e.getMessage() + ")");
    }
    t.sayHi("DifferentAccess");
  }
}
