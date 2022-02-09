/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.fhir.datacapture.validation

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.google.android.fhir.datacapture.views.localDate
import com.google.common.truth.Truth.assertThat
import java.util.Calendar
import java.util.Date
import org.hl7.fhir.r4.model.DateType
import org.hl7.fhir.r4.model.Extension
import org.hl7.fhir.r4.model.IntegerType
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.QuestionnaireResponse
import org.hl7.fhir.r4.model.StringType
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class MaxValueConstraintValidatorTest {

  lateinit var context: Context

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext()
  }

  @Test
  fun shouldReturnInvalidResult() {
    val questionnaireItem =
      Questionnaire.QuestionnaireItemComponent().apply {
        addExtension(
          Extension().apply {
            this.url = MAX_VALUE_EXTENSION_URL
            this.setValue(IntegerType(200000))
          }
        )
      }
    val questionnaireResponseItem =
      QuestionnaireResponse.QuestionnaireResponseItemComponent().apply {
        addAnswer(
          QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent().apply {
            value = IntegerType(200001)
          }
        )
      }

    val validationResult =
      MaxValueConstraintValidator.validate(questionnaireItem, questionnaireResponseItem, context)

    assertThat(validationResult.isValid).isFalse()
    assertThat(validationResult.message).isEqualTo("Maximum value allowed is:200000")
  }

  @Test
  fun shouldReturnInvalidResultDate() {
    val c: Calendar = Calendar.getInstance()
    c.time = Date()
    c.add(Calendar.DATE, 600)
    val questionnaireItem =
      Questionnaire.QuestionnaireItemComponent().apply {
        addExtension(
          Extension().apply {
            this.url = MAX_VALUE_EXTENSION_URL
            this.setValue(StringType("today()"))
          }
        )
      }
    val questionnaireResponseItem =
      QuestionnaireResponse.QuestionnaireResponseItemComponent().apply {
        addAnswer(
          QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent().apply {
            value = DateType(c.time)
          }
        )
      }

    val validationResult =
      MaxValueConstraintValidator.validate(questionnaireItem, questionnaireResponseItem, context)

    assertThat(validationResult.isValid).isFalse()
    assertThat(validationResult.message)
      .isEqualTo("Maximum value allowed is:" + DateType(Date()).localDate.toString())
  }

  @Test
  fun shouldReturnValidResultDate() {
    val questionnaireItem =
      Questionnaire.QuestionnaireItemComponent().apply {
        addExtension(
          Extension().apply {
            this.url = MAX_VALUE_EXTENSION_URL
            this.setValue(StringType("today()"))
          }
        )
      }
    val questionnaireResponseItem =
      QuestionnaireResponse.QuestionnaireResponseItemComponent().apply {
        addAnswer(
          QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent().apply {
            value = DateType(2021, 0, 1)
          }
        )
      }

    val validationResult =
      MaxValueConstraintValidator.validate(questionnaireItem, questionnaireResponseItem, context)

    assertThat(validationResult.message.isNullOrBlank()).isTrue()
    assertThat(validationResult.isValid).isTrue()
  }

  @Test
  fun shouldReturnValidResult() {
    val questionnaireItem =
      Questionnaire.QuestionnaireItemComponent().apply {
        addExtension(
          Extension().apply {
            this.url = MAX_VALUE_EXTENSION_URL
            this.setValue(IntegerType(200000))
          }
        )
      }
    val questionnaireResponseItem =
      QuestionnaireResponse.QuestionnaireResponseItemComponent().apply {
        addAnswer(
          QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent().apply {
            value = IntegerType(501)
          }
        )
      }

    val validationResult =
      MaxValueConstraintValidator.validate(questionnaireItem, questionnaireResponseItem, context)

    assertThat(validationResult.isValid).isTrue()
    assertThat(validationResult.message.isNullOrBlank()).isTrue()
  }
}
