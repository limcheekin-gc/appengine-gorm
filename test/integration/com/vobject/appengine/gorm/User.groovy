package com.vobject.appengine.gorm

import com.google.appengine.api.datastore.Key

class User implements Serializable {
  Key id
  String email
  String password
  String name
  Long version

  String toString(){
    "${id},${email},${password},${name},${version}"
  }
}