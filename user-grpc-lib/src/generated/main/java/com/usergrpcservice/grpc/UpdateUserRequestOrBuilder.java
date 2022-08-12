// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: userservice.proto

package com.usergrpcservice.grpc;

public interface UpdateUserRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:com.usergrpcservice.service.UpdateUserRequest)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>string id = 1;</code>
   * @return The id.
   */
  java.lang.String getId();
  /**
   * <code>string id = 1;</code>
   * @return The bytes for id.
   */
  com.google.protobuf.ByteString
      getIdBytes();

  /**
   * <code>optional string first_name = 2;</code>
   * @return Whether the firstName field is set.
   */
  boolean hasFirstName();
  /**
   * <code>optional string first_name = 2;</code>
   * @return The firstName.
   */
  java.lang.String getFirstName();
  /**
   * <code>optional string first_name = 2;</code>
   * @return The bytes for firstName.
   */
  com.google.protobuf.ByteString
      getFirstNameBytes();

  /**
   * <code>optional string last_name = 3;</code>
   * @return Whether the lastName field is set.
   */
  boolean hasLastName();
  /**
   * <code>optional string last_name = 3;</code>
   * @return The lastName.
   */
  java.lang.String getLastName();
  /**
   * <code>optional string last_name = 3;</code>
   * @return The bytes for lastName.
   */
  com.google.protobuf.ByteString
      getLastNameBytes();

  /**
   * <code>optional string nickname = 4;</code>
   * @return Whether the nickname field is set.
   */
  boolean hasNickname();
  /**
   * <code>optional string nickname = 4;</code>
   * @return The nickname.
   */
  java.lang.String getNickname();
  /**
   * <code>optional string nickname = 4;</code>
   * @return The bytes for nickname.
   */
  com.google.protobuf.ByteString
      getNicknameBytes();

  /**
   * <code>optional string password = 5;</code>
   * @return Whether the password field is set.
   */
  boolean hasPassword();
  /**
   * <code>optional string password = 5;</code>
   * @return The password.
   */
  java.lang.String getPassword();
  /**
   * <code>optional string password = 5;</code>
   * @return The bytes for password.
   */
  com.google.protobuf.ByteString
      getPasswordBytes();

  /**
   * <code>optional string email = 6;</code>
   * @return Whether the email field is set.
   */
  boolean hasEmail();
  /**
   * <code>optional string email = 6;</code>
   * @return The email.
   */
  java.lang.String getEmail();
  /**
   * <code>optional string email = 6;</code>
   * @return The bytes for email.
   */
  com.google.protobuf.ByteString
      getEmailBytes();

  /**
   * <code>optional string country = 7;</code>
   * @return Whether the country field is set.
   */
  boolean hasCountry();
  /**
   * <code>optional string country = 7;</code>
   * @return The country.
   */
  java.lang.String getCountry();
  /**
   * <code>optional string country = 7;</code>
   * @return The bytes for country.
   */
  com.google.protobuf.ByteString
      getCountryBytes();
}