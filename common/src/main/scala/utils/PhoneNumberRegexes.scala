package utils

object PhoneNumberRegexes {

  val regexes = Seq(
    """.*(\d{3}-\d{4}\s\d{4})""".r,        // matches 020-7437 5708
    """.*(d{3}-\d{3}\s\d{4})""".r,         // matches 020-734 7737
    """.*(\d{5}\s\d{6})""".r,              // matches 01227 276856
    """.*(\d{5}\s\d{3}\s\d{3})""".r,       // matches 01179 028 326
    """.*(\d{4}\s\d{3}\s\d{4})""".r,       // matches 0203 745 7227
    """.*(\d{4}-\d{7})""".r,               // matches 0141-2042081
    """.*(\d{6}\s\d{5})""".r,              // matches 015394 22012
    """.*(\d{3}\s\d{3}\s\d{3}\s\d{4})""".r // matches 001 646 703 2715
  )

}
