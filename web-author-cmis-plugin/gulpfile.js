var gulp = require('gulp');

var concat = require('gulp-concat');
var Synci18n = require('sync-i18n');

var fs = require('fs');


gulp.task('prepare-package', ['i18n'], function() {
  return gulp.src(['web/*.js'])
    .pipe(concat('plugin.js'))
    .pipe(gulp.dest('target/'));
});

gulp.task('i18n', function () {
  Synci18n().generateTranslations();
});

gulp.task('default', ['i18n']);