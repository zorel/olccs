package org.zorel.olccs.models

case class User(id: String,email:String, name: String)

object User {

  def byId(id: String)={
    if(id=="1"){
      User("1","jarmstrong@omnispear.com","Jared Armstrong")
    }else{
      null
    }
  }

  def login(u: String, p: String){

  }

}