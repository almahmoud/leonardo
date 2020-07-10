package org.broadinstitute.dsde.workbench.leonardo
package db

import java.time.Instant

import LeoProfile.api._
import LeoProfile.mappedColumnImplicits._

import scala.concurrent.ExecutionContext

case class AppErrorRecord(id: Long,
                          appId: AppId,
                          errorMessage: String,
                          timestamp: Instant,
                          action: ErrorAction,
                          source: ErrorSource,
                          googleErrorCode: Option[Int])

class AppErrorTable(tag: Tag) extends Table[AppErrorRecord](tag, "APP_ERROR") {
  def id = column[Long]("id", O.AutoInc)
  def appId = column[AppId]("appId")
  def errorMessage = column[String]("errorMessage", O.Length(1024))
  def timestamp = column[Instant]("timestamp", O.SqlType("TIMESTAMP(6)"))
  def action = column[ErrorAction]("action", O.Length(254))
  def source = column[ErrorSource]("source", O.Length(254))
  def googleErrorCode = column[Option[Int]]("googleErrorCode")

  def * =
    (id, appId, errorMessage, timestamp, action, source, googleErrorCode) <> (AppErrorRecord.tupled, AppErrorRecord.unapply)
}

object appErrorQuery extends TableQuery(new AppErrorTable(_)) {

  def save(appId: AppId, error: KubernetesError): DBIO[Int] =
    appErrorQuery += AppErrorRecord(0,
                                    appId,
                                    error.errorMessage,
                                    error.timestamp,
                                    error.action,
                                    error.source,
                                    error.googleErrorCode)

  def get(appId: AppId)(implicit ec: ExecutionContext): DBIO[List[KubernetesError]] =
    appErrorQuery.filter(_.appId === appId).result map { recs =>
      val errors = recs map { rec => unmarshallAppErrorRecord(rec) }
      errors.toList
    }

  def unmarshallAppErrorRecord(appErrorRecord: AppErrorRecord): KubernetesError =
    KubernetesError(appErrorRecord.errorMessage,
                    appErrorRecord.timestamp,
                    appErrorRecord.action,
                    appErrorRecord.source,
                    appErrorRecord.googleErrorCode)

}
