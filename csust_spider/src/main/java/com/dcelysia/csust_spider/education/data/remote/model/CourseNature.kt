package com.dcelysia.csust_spider.education.data.remote.model

enum class CourseNature(val id: String, val chineseName: String) {

    OTHER("00", "其他"),
    PUBLIC_COURSE("01", "公共课"),
    PUBLIC_BASIC_COURSE("02", "公共基础课"),
    PROFESSIONAL_BASIC_COURSE("03", "专业基础课"),
    PROFESSIONAL_COURSE("04", "专业课"),
    PROFESSIONAL_ELECTIVE_COURSE("05", "专业选修课"),
    PUBLIC_ELECTIVE_COURSE("06", "公共选修课"),
    PROFESSIONAL_CORE_COURSE("07", "专业核心课"),
    PROFESSIONAL_PRACTICAL_COURSE("20", "专业集中实践");

    companion object {
        fun fromChineseName(chineseName: String): CourseNature {
            return values().find { it.chineseName == chineseName } ?: OTHER
        }
    }

}