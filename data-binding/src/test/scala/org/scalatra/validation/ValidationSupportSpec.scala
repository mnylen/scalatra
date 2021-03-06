package org.scalatra
package validation

import org.specs2.mutable.Specification
import net.liftweb.json.{DefaultFormats, Formats}
import databinding._
import scalaz._
import Scalaz._
import org.scalatra.databinding.BindingSyntax._

class WithValidation extends WithBindingFromParams {
  val notRequiredCap: Field[Int] = asInt("cap").greaterThan(100)

  val legalAge: Field[Int] = asInt("age").greaterThanOrEqualTo(18)
}


class ValidationSupportSpec extends Specification {
  implicit val formats: Formats = DefaultFormats
  import org.scalatra.util.ParamsValueReaderProperties._

  "The 'ValidationSupport' trait" should {

    "do normal binding within 'bindTo'" in {

      val ageValidatedForm = new WithValidation
      val params = Map("name" -> "John", "surname" -> "Doe", "age" -> "18")

      ageValidatedForm.bindTo(params)

      ageValidatedForm.a.value must_== params("name").toUpperCase.success
      ageValidatedForm.lower.value must_== params("surname").toLowerCase.success
      ageValidatedForm.age.value must_== 18.success

    }

    "validate only 'validatable bindings' within bindTo" in {

      val ageValidatedForm = new WithValidation
      val params = Map("name" -> "John", "surname" -> "Doe", "age" -> "15")

      ageValidatedForm.isValid must beTrue

      ageValidatedForm.bindTo(params)

      ageValidatedForm.isValid must beFalse

      ageValidatedForm.errors aka "validation error list" must have(_.name == "age")

      ageValidatedForm.legalAge.value aka "the validation result" must_== Failure(ValidationError("Age must be greater than or equal to 18", FieldName("age")))
    }


    "evaluate non-exaustive validation as 'accepted'" in {
      val formUnderTest = new WithValidation
      val params = Map("name" -> "John", "surname" -> "Doe", "age" -> "20")

      params must not haveKey ("cap")

      formUnderTest.bindTo(params)
      formUnderTest.isValid must beTrue

      formUnderTest.notRequiredCap.value must_== 0.success
      formUnderTest.notRequiredCap.isValid must beTrue
    }

  }
}


