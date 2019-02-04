'use strict';

angular.module('book')
    .controller('DetailCtrl', function ($scope, $route, $routeParams, book) {

        $scope.load = function() {
            book.get($routeParams.id, function(item) {
                $scope.detailbook = item.data;
            });
        }
        $scope.backlist = function() {
            window.history.back();
        }

        $scope.load();
    });