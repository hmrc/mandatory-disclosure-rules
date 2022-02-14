package services

import javax.inject.Inject
import scala.xml.Elem

@Inject
class DataExtraction ()() {

  def messageSpecData(xml: Elem): models.submission.MessageSpecData = ???

}