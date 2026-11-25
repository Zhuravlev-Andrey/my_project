package com.example.workout.data

import androidx.room.ColumnInfo
import androidx.room.Database
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.RoomDatabase

/*
 * Модель данных приложения «Конструктор тренировок» (Room, SQLite, работает без интернета).
 *
 * Соглашения:
 *  - Все id: Long, autoGenerate (0 = «ещё не сохранён»).
 *  - Вес всегда в килограммах (Double).
 *  - Дата и время хранятся как Long: миллисекунды с 01.01.1970 UTC (System.currentTimeMillis()).
 *    Так не нужны конвертеры типов Room.
 *  - Перечисления (enum) Room сохраняет в базе как текст автоматически.
 *  - «?» после типа означает, что значение может отсутствовать (NULL).
 *
 * Разделение: справочники → шаблон тренировок → ваши данные → журнал (факт).
 */

// ============================================================================
// ПЕРЕЧИСЛЕНИЯ
// ============================================================================

/** Тип тренажёра: определяет, как отображать и подбирать вес. */
enum class MachineType {
    /** Блочный тренажёр с плитками (Hoist, Cybex VR1, кроссовер, блоки). */
    STACK,

    /** Тренажёр, на который навешиваются блины (жим ногами 45°). */
    PLATE_LOADED,

    /** Тренажёр Смита: вес считается как блины плюс гриф. */
    SMITH,

    /** Гантели: вес указывается для одной гантели. */
    DUMBBELL,

    /** Штанга со свободным грифом. */
    BARBELL,

    /** Гиря. */
    KETTLEBELL
}

/** Роль мышечной группы в упражнении. */
enum class MuscleRole {
    /** Основная нагружаемая мышца. */
    PRIMARY,

    /** Вспомогательная мышца (работает, но не главная). */
    SECONDARY
}

// ============================================================================
// 1. СПРАВОЧНИКИ
// ============================================================================

/**
 * Тренажёр или снаряд, на котором выполняется упражнение.
 * Пример: «Hoist leg curl», «Смит», «Гантели».
 */
@Entity(tableName = "machine")
data class Machine(
    /** Уникальный номер тренажёра. */
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Название тренажёра. Пример: «Hoist shoulder press». */
    val name: String,

    /** Тип тренажёра, см. [MachineType]. */
    val type: MachineType,

    /**
     * Вес «пустого» снаряда в кг, который прибавляется к выбранному весу:
     * гриф Смита (около 7–11 кг), каретка жима ногами (около 34–57 кг).
     * Для блочных тренажёров и гантелей равен 0.
     */
    @ColumnInfo(name = "base_weight_kg")
    val baseWeightKg: Double = 0.0,

    /** Свободная заметка: настройки сиденья, особенности тренажёра. Может быть пустой. */
    val note: String? = null
)

/**
 * Один доступный вес на тренажёре (одна строка сетки весов).
 * Пример: для Hoist shoulder press значения 20, 22, 25, 28 … кг.
 */
@Entity(
    tableName = "weight_option",
    foreignKeys = [
        ForeignKey(
            entity = Machine::class,
            parentColumns = ["id"],
            childColumns = ["machine_id"],
            onDelete = ForeignKey.CASCADE // удалили тренажёр – удалилась и его сетка
        )
    ],
    indices = [
        // На одном тренажёре не может быть двух весов с одним порядковым номером.
        Index(value = ["machine_id", "position"], unique = true)
    ]
)
data class WeightOption(
    /** Уникальный номер веса. */
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Тренажёр, которому принадлежит этот вес (внешний ключ → [Machine.id]). */
    @ColumnInfo(name = "machine_id")
    val machineId: Long,

    /** Значение в кг, которое выставляется на тренажёре. Пример: 31.0. */
    @ColumnInfo(name = "value_kg")
    val valueKg: Double,

    /**
     * Порядковый номер в сетке, начиная с 1 (от лёгкого к тяжёлому).
     * По нему приложение находит «следующий вес» при прогрессии.
     */
    val position: Int,

    /** true – значение вычислено, а не подтверждено на тренажёре (как у leg curl). */
    @ColumnInfo(name = "is_calculated")
    val isCalculated: Boolean = false
)

/**
 * Мышечная группа. Пример: «Квадрицепс», «Широчайшие», «Трицепс».
 */
@Entity(tableName = "muscle_group")
data class MuscleGroup(
    /** Уникальный номер мышечной группы. */
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Название мышечной группы. */
    val name: String
)

/**
 * Упражнение. Пример: «Жим ногами 45°», «Сгибание ног».
 */
@Entity(
    tableName = "exercise",
    foreignKeys = [
        ForeignKey(
            entity = Machine::class,
            parentColumns = ["id"],
            childColumns = ["machine_id"],
            onDelete = ForeignKey.RESTRICT // нельзя удалить тренажёр, пока на нём есть упражнения
        )
    ],
    indices = [Index("machine_id")]
)
data class Exercise(
    /** Уникальный номер упражнения. */
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Тренажёр, на котором выполняется упражнение (внешний ключ → [Machine.id]). */
    @ColumnInfo(name = "machine_id")
    val machineId: Long,

    /** Название упражнения. */
    val name: String,

    /** Текст-подсказка по технике выполнения. Показывается во время тренировки. */
    val technique: String? = null,

    /** Нижняя граница рабочего диапазона повторений. Пример: 8. */
    @ColumnInfo(name = "reps_min")
    val repsMin: Int = 8,

    /** Верхняя граница рабочего диапазона повторений. Пример: 10. */
    @ColumnInfo(name = "reps_max")
    val repsMax: Int = 10,

    /** Рекомендуемый отдых между подходами в секундах (для таймера). Пример: 150. */
    @ColumnInfo(name = "rest_seconds")
    val restSeconds: Int = 90
)

/**
 * Связь «упражнение – мышечная группа» (многие ко многим).
 * Одно упражнение нагружает несколько мышц, одна мышца работает в нескольких упражнениях.
 */
@Entity(
    tableName = "exercise_muscle",
    primaryKeys = ["exercise_id", "muscle_group_id"], // пара не может повторяться
    foreignKeys = [
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exercise_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MuscleGroup::class,
            parentColumns = ["id"],
            childColumns = ["muscle_group_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("muscle_group_id")]
)
data class ExerciseMuscle(
    /** Упражнение (внешний ключ → [Exercise.id]). */
    @ColumnInfo(name = "exercise_id")
    val exerciseId: Long,

    /** Мышечная группа (внешний ключ → [MuscleGroup.id]). */
    @ColumnInfo(name = "muscle_group_id")
    val muscleGroupId: Long,

    /** Основная или вспомогательная мышца, см. [MuscleRole]. */
    val role: MuscleRole
)

// ============================================================================
// 2. КОНСТРУКТОР ТРЕНИРОВОК (ШАБЛОН)
// ============================================================================

/**
 * Программа тренировок – набор тренировок, например «Фулбади 2 раза в неделю».
 */
@Entity(tableName = "plan")
data class Plan(
    /** Уникальный номер программы. */
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Название программы. */
    val name: String
)

/**
 * Тренировка-шаблон внутри программы. Пример: «Тренировка A», «Тренировка B».
 */
@Entity(
    tableName = "workout",
    foreignKeys = [
        ForeignKey(
            entity = Plan::class,
            parentColumns = ["id"],
            childColumns = ["plan_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("plan_id")]
)
data class Workout(
    /** Уникальный номер тренировки. */
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Программа, к которой относится тренировка (внешний ключ → [Plan.id]). */
    @ColumnInfo(name = "plan_id")
    val planId: Long,

    /** Название тренировки. Пример: «Тренировка A». */
    val name: String,

    /**
     * Порядок тренировки в программе, начиная с 1 (A = 1, B = 2).
     * После завершения последней тренировки программа начинается заново.
     */
    val position: Int,

    /**
     * true – это текущая тренировка, которая откроется следующей.
     * Должна быть true только у одной тренировки программы (проверяется в коде).
     */
    @ColumnInfo(name = "is_current")
    val isCurrent: Boolean = false
)

/**
 * Схема подходов – шаблон «40% / 70% / 100% / 100%».
 * Одну схему можно использовать в разных упражнениях.
 */
@Entity(tableName = "set_scheme")
data class SetScheme(
    /** Уникальный номер схемы. */
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Название схемы. Пример: «Базовая: 2 разминки + 2 рабочих». */
    val name: String
)

/**
 * Один подход внутри схемы.
 */
@Entity(
    tableName = "set_scheme_row",
    foreignKeys = [
        ForeignKey(
            entity = SetScheme::class,
            parentColumns = ["id"],
            childColumns = ["set_scheme_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["set_scheme_id", "set_number"], unique = true)]
)
data class SetSchemeRow(
    /** Уникальный номер строки. */
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Схема, которой принадлежит подход (внешний ключ → [SetScheme.id]). */
    @ColumnInfo(name = "set_scheme_id")
    val setSchemeId: Long,

    /** Номер подхода в схеме, начиная с 1. */
    @ColumnInfo(name = "set_number")
    val setNumber: Int,

    /**
     * Доля от рабочего максимума в процентах. Пример: 40.0, 70.0, 100.0.
     * Расчётный вес округляется до ближайшего значения сетки тренажёра.
     */
    val percent: Double,

    /** Минимум повторений в подходе. Пример: 8. */
    @ColumnInfo(name = "reps_min")
    val repsMin: Int,

    /** Максимум повторений в подходе. Пример: 10 (для разминки совпадает с минимумом). */
    @ColumnInfo(name = "reps_max")
    val repsMax: Int,

    /** true – разминочный подход (не считается рабочим при прогрессии). */
    @ColumnInfo(name = "is_warmup")
    val isWarmup: Boolean = false
)

/**
 * Упражнение внутри тренировки-шаблона: что делать, в каком порядке и по какой схеме.
 * Связь «тренировка – упражнение» (многие ко многим) с дополнительными полями.
 */
@Entity(
    tableName = "workout_exercise",
    foreignKeys = [
        ForeignKey(
            entity = Workout::class,
            parentColumns = ["id"],
            childColumns = ["workout_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exercise_id"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = SetScheme::class,
            parentColumns = ["id"],
            childColumns = ["set_scheme_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["workout_id", "position"], unique = true),
        Index("exercise_id"),
        Index("set_scheme_id")
    ]
)
data class WorkoutExercise(
    /** Уникальный номер записи. */
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Тренировка-шаблон (внешний ключ → [Workout.id]). */
    @ColumnInfo(name = "workout_id")
    val workoutId: Long,

    /** Упражнение (внешний ключ → [Exercise.id]). */
    @ColumnInfo(name = "exercise_id")
    val exerciseId: Long,

    /** Схема подходов для этого упражнения (внешний ключ → [SetScheme.id]). */
    @ColumnInfo(name = "set_scheme_id")
    val setSchemeId: Long,

    /** Порядок упражнения в тренировке, начиная с 1 (от тяжёлых к лёгким). */
    val position: Int
)

// ============================================================================
// 3. ВАШИ ДАННЫЕ
// ============================================================================

/**
 * Рабочий максимум по упражнению: вес, с которым вы делаете 8–10 повторений («100%»).
 * Записи не перезаписываются, а добавляются: из истории строится график прогресса.
 * Актуальный максимум – запись с самой поздней [setOn].
 */
@Entity(
    tableName = "exercise_max",
    foreignKeys = [
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exercise_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = WeightOption::class,
            parentColumns = ["id"],
            childColumns = ["weight_option_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("exercise_id"), Index("weight_option_id")]
)
data class ExerciseMax(
    /** Уникальный номер записи. */
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Упражнение (внешний ключ → [Exercise.id]). */
    @ColumnInfo(name = "exercise_id")
    val exerciseId: Long,

    /** Вес из сетки тренажёра, который стал максимумом (внешний ключ → [WeightOption.id]). */
    @ColumnInfo(name = "weight_option_id")
    val weightOptionId: Long,

    /** Когда установлен этот максимум (миллисекунды с 01.01.1970 UTC). */
    @ColumnInfo(name = "set_on")
    val setOn: Long
)

/**
 * Правило прогрессии для упражнения: когда повышать вес.
 * Пример: если в 2 рабочих подходах выполнено 10 повторений – перейти на 1 позицию вверх по сетке.
 * Не более одного правила на упражнение.
 */
@Entity(
    tableName = "progression_rule",
    foreignKeys = [
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exercise_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ProgressionRule(
    /** Упражнение, для которого действует правило (ключ и внешний ключ → [Exercise.id]). */
    @PrimaryKey
    @ColumnInfo(name = "exercise_id")
    val exerciseId: Long,

    /** Сколько повторений нужно выполнить в подходе, чтобы считать вес освоенным. Пример: 10. */
    @ColumnInfo(name = "reps_threshold")
    val repsThreshold: Int = 10,

    /** В скольких рабочих подходах подряд нужно достичь порога. Пример: 2. */
    @ColumnInfo(name = "sets_required")
    val setsRequired: Int = 2,

    /** На сколько позиций сетки повышать вес. Пример: 1 (до следующего значения). */
    @ColumnInfo(name = "step_positions")
    val stepPositions: Int = 1
)

// ============================================================================
// 4. ЖУРНАЛ (ФАКТ)
// ============================================================================

/**
 * Проведённая тренировка (одно посещение зала).
 */
@Entity(
    tableName = "session",
    foreignKeys = [
        ForeignKey(
            entity = Workout::class,
            parentColumns = ["id"],
            childColumns = ["workout_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("workout_id")]
)
data class Session(
    /** Уникальный номер тренировки в журнале. */
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Шаблон, по которому проходила тренировка (внешний ключ → [Workout.id]). */
    @ColumnInfo(name = "workout_id")
    val workoutId: Long,

    /** Время начала (миллисекунды с 01.01.1970 UTC). Дата тренировки берётся отсюда. */
    @ColumnInfo(name = "started_at")
    val startedAt: Long,

    /** Время окончания. null – тренировка ещё не завершена. */
    @ColumnInfo(name = "finished_at")
    val finishedAt: Long? = null,

    /** Заметка о самочувствии, сне, нагрузке. Может быть пустой. */
    val note: String? = null
)

/**
 * Один выполненный подход в тренировке.
 * Вес хранится как число, а не как ссылка на сетку: если позже поправить сетку тренажёра,
 * старые записи не изменятся.
 */
@Entity(
    tableName = "session_set",
    foreignKeys = [
        ForeignKey(
            entity = Session::class,
            parentColumns = ["id"],
            childColumns = ["session_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exercise_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["session_id", "exercise_id", "set_number"], unique = true),
        Index("exercise_id")
    ]
)
data class SessionSet(
    /** Уникальный номер подхода. */
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Тренировка в журнале (внешний ключ → [Session.id]). */
    @ColumnInfo(name = "session_id")
    val sessionId: Long,

    /** Упражнение (внешний ключ → [Exercise.id]). */
    @ColumnInfo(name = "exercise_id")
    val exerciseId: Long,

    /** Номер подхода в этом упражнении, начиная с 1. */
    @ColumnInfo(name = "set_number")
    val setNumber: Int,

    /** Фактический вес в кг (для гантелей – одной гантели). */
    @ColumnInfo(name = "weight_kg")
    val weightKg: Double,

    /** Сколько повторений выполнено. */
    val reps: Int,

    /**
     * Запас повторений (RIR): сколько ещё можно было сделать.
     * 0 – до отказа, 2 – оставался запас в 2 повтора. null – не указано.
     */
    val rir: Int? = null,

    /** true – разминочный подход (не учитывается при прогрессии). */
    @ColumnInfo(name = "is_warmup")
    val isWarmup: Boolean = false
)

// ============================================================================
// БАЗА ДАННЫХ
// ============================================================================

/**
 * Точка входа в базу. Версию [version] повышайте при каждом изменении таблиц
 * и добавляйте миграцию, иначе данные пользователя потеряются.
 * Для запросов к таблицам нужно добавить интерфейсы Dao (отдельный шаг).
 */
@Database(
    entities = [
        Machine::class,
        WeightOption::class,
        MuscleGroup::class,
        Exercise::class,
        ExerciseMuscle::class,
        Plan::class,
        Workout::class,
        SetScheme::class,
        SetSchemeRow::class,
        WorkoutExercise::class,
        ExerciseMax::class,
        ProgressionRule::class,
        Session::class,
        SessionSet::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase()