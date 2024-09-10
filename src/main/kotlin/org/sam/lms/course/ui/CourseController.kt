package org.sam.lms.course.ui

import org.sam.lms.course.application.CourseService
import org.sam.lms.course.application.payload.CreateCourseDto
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/courses")
class CourseController(private val courseService: CourseService) {

    @PostMapping
    fun create(@RequestBody createCourseDto: CreateCourseDto) {
        this.courseService.create(createCourseDto)
    }

}