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

import org.calypsonet.terminal.reader.CardReader;
import org.calypsonet.terminal.reader.selection.spi.SmartCard;


/**
 * Card extension service providing basic access to APDU exchange functions with a card.
 *
 * @since 2.0.0
 */
public final class GenericExtensionService {

  private static final GenericExtensionService INSTANCE = new GenericExtensionService();

  /** Constructor */
  private GenericExtensionService() {}

  /**
   * Gets the unique instance of this object.
   *
   * @return A not null reference.
   */
  public static GenericExtensionService getInstance() {
    return INSTANCE;
  }

  /**
   * Creates an instance of {@link GenericCardSelection}.
   *
   * @return A not null reference.
   * @since 2.0.0
   */
  public GenericCardSelection createCardSelection() {
    return new GenericCardSelectionAdapter();
  }

  /**
   * Creates an instance of {@link GenericCardTransactionManager}.
   *
   * @param reader The reader through which the card communicates.
   * @param card The initial card data provided by the selection process.
   * @return A not null reference.
   * @since 2.0.0
   */
  public GenericCardTransactionManager createCardTransaction(CardReader reader, SmartCard card) {
    return new GenericCardTransactionManagerAdapter(reader, card);
  }

}
