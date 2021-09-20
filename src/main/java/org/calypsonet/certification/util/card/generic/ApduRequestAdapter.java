/* **************************************************************************************
 * Copyright (c) 2021 Calypso Networks Association https://calypsonet.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.calypsonet.certification.util.card.generic;

import java.util.HashSet;
import java.util.Set;
import org.calypsonet.terminal.card.spi.ApduRequestSpi;

/**
 * (package-private)<br>
 * Implementation of {@link ApduRequestSpi}
 *
 * @since 2.0.0
 */
class ApduRequestAdapter implements ApduRequestSpi {
  private static final int DEFAULT_SUCCESSFUL_CODE = 0x9000;

  private final byte[] apdu;
  private final Set<Integer> successfulStatusWords;
  private String info;

  /**
   * Builds an APDU request from a raw byte buffer.
   *
   * <p>The default status words list is initialized with the standard successful code 9000h.
   *
   * @param apdu The bytes of the APDU's body.
   * @since 2.0.0
   */
  public ApduRequestAdapter(byte[] apdu) {
    this.apdu = apdu;
    this.successfulStatusWords = new HashSet<Integer>();
    this.successfulStatusWords.add(DEFAULT_SUCCESSFUL_CODE);
  }

  /**
   * Adds a status word to the list of those that should be considered successful for the APDU.
   *
   * <p>Note: initially, the list contains the standard successful status word {@code 9000h}.
   *
   * @param successfulStatusWord A positive int &le; {@code FFFFh}.
   * @return The current instance.
   * @since 2.0.0
   */
  public ApduRequestAdapter addSuccessfulStatusWord(int successfulStatusWord) {
    this.successfulStatusWords.add(successfulStatusWord);
    return this;
  }

  /**
   * Information about the APDU request.
   *
   * <p>This string is dedicated to improve the readability of logs and should therefore only be
   * invoked conditionally (e.g. when log level &gt;= debug).
   *
   * @param info The request info (free text).
   * @return The current instance.
   * @since 2.0.0
   */
  public ApduRequestAdapter setInfo(final String info) {
    this.info = info;
    return this;
  }

  /**
   * {@inheritDoc}
   *
   * @since 2.0.0
   */
  @Override
  public byte[] getApdu() {
    return this.apdu;
  }

  /**
   * {@inheritDoc}
   *
   * @since 2.0.0
   */
  @Override
  public Set<Integer> getSuccessfulStatusWords() {
    return successfulStatusWords;
  }

  /**
   * {@inheritDoc}
   *
   * @since 2.0.0
   */
  @Override
  public String getInfo() {
    return info;
  }
}
