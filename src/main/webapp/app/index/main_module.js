var mainModule = angular.module("mainModule", ['taskModule']);

mainModule.controller('IndexCtrl', function TasksCtrl($scope, TaskFacade) {
        
    TaskFacade.findAll().success(function(data, status, headers, config) {
        $scope.tasks = data;
    }).error(function(data, status, headers, config) {
        
        console.log("Ошибка при получении задач. Status: "+status, '; Data: '+data);

    });
});

