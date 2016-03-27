(function (window, angular, $) {
    'use strict';
    angular.module('FileManagerApp').controller('FileManagerCtrl', [
        '$scope', '$translate', '$cookies', 'fileManagerConfig', 'item', 'fileNavigator', 'fileUploader',
        function ($scope, $translate, $cookies, fileManagerConfig, Item, FileNavigator, FileUploader) {

            $scope.config = fileManagerConfig;
            $scope.reverse = false;
            $scope.predicate = ['model.type', 'model.name'];
            $scope.order = function (predicate) {
                $scope.reverse = ($scope.predicate[1] === predicate) ? !$scope.reverse : false;
                $scope.predicate[1] = predicate;
            };

            $scope.query = '';
            $scope.temp = new Item();
            $scope.fileNavigator = new FileNavigator();
            $scope.fileUploader = FileUploader;
            $scope.uploadFileList = [];
            $scope.viewTemplate = $cookies.viewTemplate || 'main-table.html';

            $scope.setTemplate = function (name) {
                $scope.viewTemplate = $cookies.viewTemplate = name;
            };

            $scope.changeLanguage = function (locale) {
                if (locale) {
                    return $translate.use($cookies.language = locale);
                }
                $translate.use($cookies.language || fileManagerConfig.defaultLang);
            };

            $scope.touch = function (item) {
                item = item instanceof Item ? item : new Item();
                item.revert();
                $scope.temp = item;
            };

            $scope.smartClick = function (item, e) {
                if (item.isFolder()) {
                    return $scope.fileNavigator.folderClick(item);
                }
                if (item.isImage()) {
                    return $scope.openImagePreview(item);
                }
                if (item.isEditable()) {
                    return $scope.openEditItem(item);
                }
            };

            $scope.openImagePreview = function (item) {
                item.inprocess = true;
                $scope.modal('imagepreview')
                    .find('#imagepreview-target')
                    .attr('src', item.getUrl(true))
                    .unbind('load error')
                    .on('load error', function () {
                        item.inprocess = false;
                        $scope.$apply();
                    });
                return $scope.touch(item);
            };

            $scope.startUploadFile = function (e) {
                //console.log(111);
                //console.log(extraObj);
                extraObj.startUpload();
            };

            $scope.genQRcode = function () {
                var qrcode = $("#qrcode");
                $("#qrcode").html("");


                var ulObj = $("<ul />", {
                    "class": "nav nav-tabs"
                }).appendTo(qrcode);

                var liClass = "";
                for (var i = 0; i < serverAddresses.length; i++) {
                    if (i == 0) {
                        liClass = "active";
                    } else {
                        liClass = "";
                    }
                    var liObj = $("<li />", {
                        "class": liClass
                    }).appendTo(ulObj);

                    $("<a /> ", {
                        "href": "#qrcontent" + i,
                        "data-toggle": "tab",
                        "text": "地址" + (i + 1)
                    }).appendTo(liObj);
                }
                var contentClass = "";
                var contentObj = $("<div />", {
                    "class": "tab-content"
                }).appendTo(qrcode);
                for (var i = 0; i < serverAddresses.length; i++) {
                    if (i == 0) {
                        contentClass = "active";
                    } else {
                        contentClass = ""
                    }
                    var obj = $("<div />", {
                        "class": " qrcode tab-pane fade in " + contentClass,
                        "id": "qrcontent" + i
                    }).appendTo(contentObj);

                    $("<div />", {
                        "id": "qrcode" + i
                    }).appendTo(obj);

                    $('#qrcode' + i).qrcode(serverAddresses[i]);
                }
            }

            $scope.clearUploadFileList = function (e) {
                //console.log("clearUploadFileList");
                $(".ajax-file-upload-container").html("");
            };

            $scope.openEditItem = function (item) {
                item.getContent();
                $scope.modal('edit');
                return $scope.touch(item);
            };

            $scope.modal = function (id, hide) {
                return $('#' + id).modal(hide ? 'hide' : 'show');
            };

            $scope.isInThisPath = function (path) {
                var currentPath = $scope.fileNavigator.currentPath.join('/');
                return currentPath.indexOf(path) !== -1;
            };

            $scope.edit = function (item) {
                item.edit().then(function () {
                    $scope.modal('edit', true);
                });
            };

            $scope.changePermissions = function (item) {
                item.changePermissions().then(function () {
                    $scope.modal('changepermissions', true);
                });
            };

            $scope.copy = function (item) {
                var samePath = item.tempModel.path.join() === item.model.path.join();
                if (samePath && $scope.fileNavigator.fileNameExists(item.tempModel.name)) {
                    item.error = $translate.instant('error_invalid_filename');
                    return false;
                }
                item.copy().then(function () {
                    $scope.fileNavigator.refresh();
                    $scope.modal('copy', true);
                });
            };

            $scope.compress = function (item) {
                item.compress().then(function () {
                    $scope.fileNavigator.refresh();
                    if (!$scope.config.compressAsync) {
                        return $scope.modal('compress', true);
                    }
                    item.asyncSuccess = true;
                }, function () {
                    item.asyncSuccess = false;
                });
            };

            $scope.extract = function (item) {
                item.extract().then(function () {
                    $scope.fileNavigator.refresh();
                    if (!$scope.config.extractAsync) {
                        return $scope.modal('extract', true);
                    }
                    item.asyncSuccess = true;
                }, function () {
                    item.asyncSuccess = false;
                });
            };

            $scope.remove = function (item) {
                item.remove().then(function () {
                    $scope.fileNavigator.refresh();
                    $scope.modal('delete', true);
                });
            };

            $scope.rename = function (item) {
                var samePath = item.tempModel.path.join() === item.model.path.join();
                if (samePath && $scope.fileNavigator.fileNameExists(item.tempModel.name)) {
                    item.error = $translate.instant('error_invalid_filename');
                    return false;
                }
                item.rename().then(function () {
                    $scope.fileNavigator.refresh();
                    $scope.modal('rename', true);
                });
            };

            $scope.createFolder = function (item) {
                var name = item.tempModel.name && item.tempModel.name.trim();
                item.tempModel.type = 'dir';
                item.tempModel.path = $scope.fileNavigator.currentPath;
                if (name && !$scope.fileNavigator.fileNameExists(name)) {
                    item.createFolder().then(function () {
                        $scope.fileNavigator.refresh();
                        $scope.modal('newfolder', true);
                    });
                } else {
                    item.error = $translate.instant('error_invalid_filename');
                    return false;
                }
            };

            $scope.uploadFiles = function () {
                $scope.fileUploader.upload($scope.uploadFileList, $scope.fileNavigator.currentPath).then(function () {
                    $scope.fileNavigator.refresh();
                    $scope.modal('uploadfile', true);
                }, function (data) {
                    var errorMsg = data.result && data.result.error || $translate.instant('error_uploading_files');
                    $scope.temp.error = errorMsg;
                });
            };

            $scope.getQueryParam = function (param) {
                var found;
                window.location.search.substr(1).split('&').forEach(function (item) {
                    if (param === item.split('=')[0]) {
                        found = item.split('=')[1];
                        return false;
                    }
                });
                return found;
            };

            $scope.changeLanguage($scope.getQueryParam('lang'));
            $scope.isWindows = $scope.getQueryParam('server') === 'Windows';
            $scope.fileNavigator.refresh();
        }]);
})(window, angular, jQuery);
