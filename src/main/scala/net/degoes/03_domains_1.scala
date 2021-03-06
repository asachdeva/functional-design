package net.degoes

import ch.qos.logback.classic.spi.PackagingDataCalculator

/*
 * INTRODUCTION
 *
 * In Functional Design, a functional domain consists of three things:
 *
 * 1. A set of types that model a solution to a domain problem.
 *
 * 2. Constructors that allow constructing simple solutions.
 *
 * 3. Operators that solving more complex problems by transforming
 *    and combining solutions for subproblems.
 *
 * Functional domains allow modeling solutions to problems in a specific domain.
 * Done properly, a small set of primitives can be so powerful, they can be used
 * compositionally to describe all possible solutions in that domain.
 *
 * A functional domain can be regarded as a type of internal domain-specific
 * language (DSL), which is designed specifically for expressing compositional
 * solutions to some category of domain problems.
 *
 * ZIO is an example of a domain for input/output, whose effect type lets you
 * solve async/concurrent/resourceful problems, and whose operators let you
 * assemble large solutions from small solutions.
 *
 * In this section, you'll learn about designing domains using ADTS,
 * constructors, and composable operators.
 */

/**
 * SPREADSHEET - EXERCISE SET 1
 *
 * Consider a spreadsheet application with a bunch of cells, containing either
 * static data or formula computed from other cells.
 */
object spreadsheet {
  trait Spreadsheet {
    def cols: Int
    def rows: Int

    def valueAt(col: Int, row: Int): CellContents

    final def scan(range: Range): Stream[Cell] = {
      val minRow = range.minRow.getOrElse(0)
      val maxRow = range.maxRow.getOrElse(rows - 1)

      val minCol = range.minCol.getOrElse(0)
      val maxCol = range.maxCol.getOrElse(cols - 1)

      (for {
        col <- (minCol to maxCol).toStream
        row <- (minRow to maxRow).toStream
      } yield Cell(col, row, valueAt(col, row)))
    }
  }

  final case class Range(minRow: Option[Int], maxRow: Option[Int], minCol: Option[Int], maxCol: Option[Int])
  object Range {
    def column(i: Int): Range = Range(None, None, Some(i), Some(i))

    def row(i: Int): Range = Range(Some(i), Some(i), None, None)
  }

  final case class Cell(col: Int, row: Int, contents: CellContents)

  sealed trait CellContents
  object CellContents {
    final case class Error(message: String) extends CellContents
    final case class Str(value: String)     extends CellContents
    final case class Dbl(value: Double)     extends CellContents

    /**
     * EXERCISE 1
     *
     * Design a subtype of `CellContents` called `CalculatedValue`, which
     * represents a value that is dynamically computed from a spreadsheet.
     */
    final case class CalculatedValue(evaluate: Spreadsheet => CellContents) extends CellContents { self =>

      /**
       * EXERCISE 2
       *
       * Add some operators to transform one `CalculatedValue` into another `CalculatedValue`. For
       * example, one operator could "negate" a double expression.
       */
      def negate: CalculatedValue =
        CalculatedValue { spreadsheet =>
          self.evaluate(spreadsheet) match {
            case Dbl(value) => Dbl(-value)
            case x          => Error(s"Expected Double but found $x")
          }
        }

      /**
       * EXERCISE 3
       *
       * Add some operators to combine `CalculatedValue`. For example, one operator
       * could sum two double expressions.
       */
      def sum(that: CalculatedValue): CalculatedValue = CalculatedValue { spreadsheet =>
        val left  = self.evaluate(spreadsheet)
        val right = self.evaluate(spreadsheet)

        (left, right) match {
          case (Dbl(left), Dbl(right)) => Dbl(left + right)
          case (x, y)                  => Error(s"Cannot add the values $x and $y")
        }
      }
    }
    object CalculatedValue {

      /**
       * EXERCISE 4
       *
       * Add a constructor that makes an CalculatedValue from a CellContents.
       */
      def const(contents: CellContents): CalculatedValue = contents match {
        case Dbl(value)     => CalculatedValue(_ => Dbl(value))
        case Error(message) => CalculatedValue(_ => Error(message))
        case Str(value)     => CalculatedValue(_ => Str(value))
        case x              => CalculatedValue(_ => Error(s"Cannot convert $x to CalculatedValue"))
      }

      /**
       * EXERCISE 5
       *
       * Add a constructor that provides access to the value of the
       * specified cell, identified by col/row.
       */
      def at(col: Int, row: Int): CalculatedValue = ???

      def fromDouble(d: Double): CalculatedValue = CalculatedValue(_ => Dbl(d))
    }
  }

  /**
   * EXERCISE 6
   *
   * Describe a cell whose contents are the sum of other cells.
   */
  lazy val cell1: Cell = ???
}

/**
 * ETL - EXERCISE SET 2
 *
 * Consider an application designed to extract, transform, and load data.
 */
object etl {
  import scala.util._

  /**
   * Represents a row of data.
   */
  final case class DataRow(row: Map[String, DataValue]) {
    def delete(name: String): DataRow = DataRow(row - name)

    def rename(oldName: String, newName: String): DataRow =
      DataRow(row.get(oldName).fold(row)(value => (row - oldName).updated(newName, value)))
  }

  /**
   * Represents a stream of data.
   */
  final case class DataStream(foreach: (Try[DataRow] => Unit) => Unit) { self =>
    def delete(name: String): DataStream = map(_.delete(name))

    def orElse(that: => DataStream): DataStream =
      DataStream { callback =>
        self.foreach {
          case Failure(exception) => that.foreach(callback)
          case x                  => callback(x)
        }
      }

    def map(f: DataRow => DataRow): DataStream =
      DataStream(callback => self.foreach(a => callback(a.map(f))))

    def rename(oldName: String, newName: String): DataStream =
      self.map(_.rename(oldName, newName))
  }

  /**
   * EXERCISE 1
   *
   * Design a data type that models sources and sinks in an ETL pipeline. Assume
   * your business requires you to extract data from (and load data to) FTP sites,
   * URLs, AWS S3 buckets, and databases described by JDBC connection strings.
   *
   * Also mock out, but do not implement, a method on each repository type called
   * `load`, which returns a `DataStream`.
   */
  sealed trait DataRepo
  object DataRepo {
    final case class URL(url: String)                 extends DataRepo
    final case class FTP(host: String, port: Int)     extends DataRepo
    final case class S3(url: String, bucket: String)  extends DataRepo
    final case class Database(jdbcConnection: String) extends DataRepo
  }

  /**
   * EXERCISE 2
   *
   * Design a data type that models the type of primitives the ETL pipeline
   * has access to. This will include string, numeric, and date/time data.
   */
  sealed trait DataType
  object DataType {
    final case object Str      extends DataType
    final case object Integer  extends DataType
    final case object DateTime extends DataType
    final case object Bool     extends DataType
  }

  /**
   * EXERCISE 3
   *
   * Design a data type that models a value. Every value should have a `DataType`
   * that identifies its type (string, numeric, or data/time), and a `coerce` method
   * to coerce the value into another type.
   *
   * Be sure to model null, string, and integer, at the very least!
   */
  sealed trait DataValue {
    def dataType: DataType

    //def coerce(otherType: DataType): Option[DataValue]
  }

  object DataValue {
    abstract class AbstractDataValue(val dataType: DataType) extends DataValue
    final case class Integer(i: Int)                         extends AbstractDataValue(DataType.Integer)
    final case class DateTime(t: java.time.LocalDateTime)    extends AbstractDataValue(DataType.DateTime)
    final case class Bool(b: Boolean)                        extends AbstractDataValue(DataType.Bool)
    final case class Str(s: String)                          extends AbstractDataValue(DataType.Str)
  }

  /**
   * `Pipeline` is a data type that models a transformation from an input data
   * set into an output data step, as a series of one or more individual
   * operations.
   */
  case class Pipeline(run: () => DataStream) { self =>

    def +(that: Pipeline): Pipeline = Pipeline.Sequential(self, that)

    /**
     * EXERCISE 4
     *
     * Add a `merge` operator that models the merge of the output of this
     * pipeline with the output of the specified pipeline.
     */
    def merge(that: Pipeline): Pipeline = ???

    /**
     * EXERCISE 5
     *
     * Add an `orElse` operator that models applying this pipeline, but if it
     * fails, switching over and trying another pipeline.
     */
    def orElse(that: Pipeline): Pipeline = ???

    /**
     * EXERCISE 6
     *
     * Add an operator to rename a column in a pipeline.
     */
    def rename(oldName: String, newName: String): Pipeline = ???

    /**
     * EXERCISE 7
     *
     * Add an operator to coerce a column into a specific type in a pipeline.
     */
    def coerce(column: String, newType: DataType): Pipeline = ???

    /**
     * EXERCISE 8
     *
     * Add an operator to delete a column in a pipeline.
     */
    def delete(column: String): Pipeline = ???

    /**
     * EXERCISE 9
     *
     * To replace nulls in the specified column with a specified value.
     */
    def replaceNulls(column: String, defaultValue: DataValue): Pipeline = ???
  }

  object Pipeline {
    //final case class Extract(dataRepo: DataRepo)                                               extends Pipeline
    //final case class Sequential(first: Pipeline, second: Pipeline)                             extends Pipeline
    //final case class Fallback(first: Pipeline, second: Pipeline)                               extends Pipeline
    //final case class Rename(pipeline: Pipeline, oldName: String, newName: String)              extends Pipeline
    //final case class Coerce(pipeline: Pipeline, name: String, newType: DataType)               extends Pipeline
    //final case class Delete(pipeline: Pipeline, column: String)                                extends Pipeline
    //final case class ReplaceNulls(pipeline: Pipeline, column: String, defaultValue: DataValue) extends Pipeline
    //final case class Load(pipeline: Pipeline, repo: DataRepo)                                  extends Pipeline

    /**
     * EXERCISE 10
     *
     * Add a constructor for `Pipeline` that models extraction of data from
     * the specified data repository.
     */
    def extract(repo: DataRepo): Pipeline = ???
  }

  /**
   * EXERCISE 11
   *
   * Create a pipeline that models extracts data from a URL, replacing all null
   * "age" columns with "0" as the default age, which renames a column "fname"
   * into a column "first_name", and which coerces the "age" column into an
   * integer type.
   */
  lazy val pipeline: Pipeline = ???
}

/**
 * REAL ESTATE APP - GRADUATION PROJECT
 *
 * Consider a real estate app that must regularly fetch third-party pricing data
 * according to specified schedules. These schedules can be quite complicated,
 * although they possess regular structure (e.g. every fifth Tuesday, and hourly
 * on Wednesdays). The business considers it acceptable to create the schedules in
 * code (rather than reading them from a database).
 */
object pricing_fetcher {
  def fetch(directory: java.io.File, url: java.net.URL, schedule: Schedule): Unit = ???

  sealed trait DayOfWeek
  object DayOfWeek {
    case object Sunday    extends DayOfWeek
    case object Monday    extends DayOfWeek
    case object Tuesday   extends DayOfWeek
    case object Wednesday extends DayOfWeek
    case object Thursday  extends DayOfWeek
    case object Friday    extends DayOfWeek
    case object Saturday  extends DayOfWeek
  }

  final case class Time(minuteOfHour: Int, hourOfDay: Int, dayOfWeek: DayOfWeek, weekOfMonth: Int, monthOfYear: Int)

  /**
   * `Schedule` is a data type that models a schedule as a simple function,
   * which specifies whether or not it is time to perform a fetch.
   */
  sealed trait Schedule { self =>
    //final case class Schedule(fetchNow: Time => Boolean) { self =>
    import Schedule._
    /*
     * EXERCISE 1
     *
     * Create an operator for schedule that allows composing two schedules to
     * yield the union of those schedules. That is, the fetch will occur
     * only when either of the schedules would have performed a fetch.
     */
    def union(that: Schedule): Schedule = Union(self, that)

    /**
     * EXERCISE 2
     *
     * Create an operator for schedule that allows composing two schedules to
     * yield the intersection of those schedules. That is, the fetch will occur
     * only when both of the schedules would have performed a fetch.
     */
    def intersection(that: Schedule): Schedule = Interleave(self, that)

    /**
     * EXERCISE 3
     *
     * Create a unary operator that returns a schedule that will never fetch
     * when the original schedule would fetch, and will always fetch when the
     * original schedule would not fetch.
     */
    def negate: Schedule = ???
  }

  object Schedule {
    final case class Weeks(value: List[Int])                     extends Schedule
    final case class DaysOfTheWeek(value: List[Int])             extends Schedule
    final case class Hours(value: List[Int])                     extends Schedule
    final case class Minutes(value: List[Int])                   extends Schedule
    final case class Times(schedule: Schedule, n: Int)           extends Schedule
    final case class Union(left: Schedule, right: Schedule)      extends Schedule
    final case class Interleave(left: Schedule, right: Schedule) extends Schedule

    /**
     * EXERCISE 4
     *
     * Create a constructor for Schedule that models fetching on specific weeks
     * of the month.
     */
    def weeks(weeks: Int*): Schedule = Weeks(weeks.toList)

    /**
     * EXERCISE 5
     *
     * Create a constructor for Schedule that models fetching on specific days
     * of the week.
     */
    def daysOfTheWeek(daysOfTheWeek: Int*): Schedule = DaysOfTheWeek(daysOfTheWeek.toList)

    /**
     * EXERCISE 6
     *
     * Create a constructor for Schedule that models fetching on specific
     * hours of the day.
     */
    def hoursOfTheDay(hours: Int*): Schedule = Hours(hours.toList)

    /**
     * EXERCISE 7
     *
     * Create a constructor for Schedule that models fetching on specific minutes
     * of the hour.
     */
    def minutesOfTheHour(minutes: Int*): Schedule = Minutes(minutes.toList)
  }

  /**
   * EXERCISE 8
   *
   * Create a schedule that repeats every Wednesday, at 6:00 AM and 12:00 PM,
   * and at 5:30, 6:30, and 7:30 every Thursday.
   */
  import Schedule._
  lazy val schedule: Schedule = Schedule
    .daysOfTheWeek(3)
    .union(hoursOfTheDay(6, 12))
    .union(daysOfTheWeek(4).union(hoursOfTheDay(5, 7).union(minutesOfTheHour(30))))
}
