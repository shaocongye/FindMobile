/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.touchthink.obedient;

public class Config {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected Config(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(Config obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        pocketsphinxJNI.delete_Config(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public Config() {
    this(pocketsphinxJNI.new_Config__SWIG_0(), true);
  }

  public Config(String file) {
    this(pocketsphinxJNI.new_Config__SWIG_1(file), true);
  }

  public void setBoolean(String key, boolean val) {
    pocketsphinxJNI.Config_setBoolean(swigCPtr, this, key, val);
  }

  public void setInt(String key, int val) {
    pocketsphinxJNI.Config_setInt(swigCPtr, this, key, val);
  }

  public void setFloat(String key, double val) {
    pocketsphinxJNI.Config_setFloat(swigCPtr, this, key, val);
  }

  public void setString(String key, String val) {
    pocketsphinxJNI.Config_setString(swigCPtr, this, key, val);
  }

  public boolean exists(String key) {
    return pocketsphinxJNI.Config_exists(swigCPtr, this, key);
  }

  public boolean getBoolean(String key) {
    return pocketsphinxJNI.Config_getBoolean(swigCPtr, this, key);
  }

  public int getInt(String key) {
    return pocketsphinxJNI.Config_getInt(swigCPtr, this, key);
  }

  public double getFloat(String key) {
    return pocketsphinxJNI.Config_getFloat(swigCPtr, this, key);
  }

  public String getString(String key) {
    return pocketsphinxJNI.Config_getString(swigCPtr, this, key);
  }

}
