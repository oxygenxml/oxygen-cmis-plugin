var gulp = require('gulp');

var concat = require('gulp-concat');
var Synci18n = require('sync-i18n');
var iife = require("gulp-iife");
var uglify = require('gulp-uglify');


var fs = require('fs');


gulp.task('prepare-package', ['i18n'], function() {
  return gulp.src(['web/*.js'])
    .pipe(concat('plugin.js'))
    .pipe(iife({useStrict: false, prependSemicolon: true}))
    .pipe(uglify())
    .pipe(gulp.dest('target/'));
});

gulp.task('i18n', function () {
  Synci18n().generateTranslations();
});

gulp.task('default', ['i18n']);
