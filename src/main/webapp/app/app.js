var app = angular.module('tenderRobot', [
    'mainModule', 
    'taskModule', 
    'ngRoute']);

app.config(function($routeProvider) {
    $routeProvider.
    when('/', {
        templateUrl: "app/index/index.tpl.html"
    }).when('/task', {
        templateUrl: "app/task/task.tpl.html"
    }).when('/auction', {
        templateUrl: "app/auction/auction.tpl.html"
    }).when('/request', {
        templateUrl: "app/request/request.tpl.html"
    });
});