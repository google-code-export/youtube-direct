/* Copyright (c) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// namespace protection against collision
var admin = admin || {};
admin.sub = admin.sub || {};

admin.sub.languageMap = {
				"aa": "Afar",
				"ab": "Abkhazian",
				"ae": "Avestan",
				"af": "Afrikaans",
				"ak": "Akan",
				"am": "Amharic",
				"an": "Aragonese",
				"ar": "Arabic",
				"as": "Assamese",
				"av": "Avaric",
				"ay": "Aymara",
				"az": "Azerbaijani",
				"ba": "Bashkir",
				"be": "Belarusian",
				"bg": "Bulgarian",
				"bh": "Bihari",
				"bi": "Bislama",
				"bm": "Bambara",
				"bn": "Bengali",
				"bo": "Tibetan",
				"br": "Breton",
				"bs": "Bosnian",
				"ca": "Catalan",
				"ce": "Chechen",
				"ch": "Chamorro",
				"co": "Corsican",
				"cr": "Cree",
				"cs": "Czech",
				"cu": "Church Slavic",
				"cv": "Chuvash",
				"cy": "Welsh",
				"da": "Danish",
				"de": "German",
				"dv": "Divehi",
				"dz": "Dzongkha",
				"ee": "Ewe",
				"el": "Greek",
				"en": "English",
				"eo": "Esperanto",
				"es": "Spanish",
				"et": "Estonian",
				"eu": "Basque",
				"fa": "Persian",
				"ff": "Fulah",
				"fi": "Finnish",
				"fj": "Fijian",
				"fo": "Faroese",
				"fr": "French",
				"fy": "Western Frisian",
				"ga": "Irish",
				"gd": "Gaelic",
				"gl": "Galician",
				"gn": "Guarani",
				"gu": "Gujarati",
				"gv": "Manx",
				"ha": "Hausa",
				"he": "Hebrew",
				"hi": "Hindi",
				"ho": "Hiri Motu",
				"hr": "Croatian",
				"ht": "Haitian",
				"hu": "Hungarian",
				"hy": "Armenian",
				"hz": "Herero",
				"ia": "Interlingua",
				"id": "Indonesian",
				"ie": "Interlingue",
				"ig": "Igbo",
				"ii": "Sichuan Yi",
				"ik": "Inupiaq",
				"io": "Ido",
				"is": "Icelandic",
				"it": "Italian",
				"iu": "Inuktitut",
				"ja": "Japanese",
				"jv": "Javanese",
				"ka": "Georgian",
				"kg": "Kongo",
				"ki": "Kikuyu",
				"kj": "Kuanyama",
				"kk": "Kazakh",
				"kl": "Kalaallisut",
				"km": "Central Khmer",
				"kn": "Kannada",
				"ko": "Korean",
				"kr": "Kanuri",
				"ks": "Kashmiri",
				"ku": "Kurdish",
				"kv": "Komi",
				"kw": "Cornish",
				"ky": "Kirghiz",
				"la": "Latin",
				"lb": "Luxembourgish",
				"lg": "Ganda",
				"li": "Limburgan",
				"ln": "Lingala",
				"lo": "Lao",
				"lt": "Lithuanian",
				"lu": "Luba-Katanga",
				"lv": "Latvian",
				"mg": "Malagasy",
				"mh": "Marshallese",
				"mi": "Maori",
				"mk": "Macedonian",
				"ml": "Malayalam",
				"mn": "Mongolian",
				"mr": "Marathi",
				"ms": "Malay",
				"mt": "Maltese",
				"my": "Burmese",
				"na": "Nauru",
				"nb": "Norwegian Bokmål",
				"nd": "Ndebele, North",
				"ne": "Nepali",
				"ng": "Ndonga",
				"nl": "Dutch",
				"nn": "Norwegian Nynorsk",
				"no": "Norwegian",
				"nr": "Ndebele, South",
				"nv": "Navajo",
				"ny": "Chichewa",
				"oc": "Occitan",
				"oj": "Ojibwa",
				"om": "Oromo",
				"or": "Oriya",
				"os": "Ossetian",
				"pa": "Panjabi",
				"pi": "Pali",
				"pl": "Polish",
				"ps": "Pushto",
				"pt": "Portuguese",
				"qu": "Quechua",
				"rm": "Romansh",
				"rn": "Rundi",
				"ro": "Romanian",
				"ru": "Russian",
				"rw": "Kinyarwanda",
				"sa": "Sanskrit",
				"sc": "Sardinian",
				"sd": "Sindhi",
				"se": "Northern Sami",
				"sg": "Sango",
				"si": "Sinhala",
				"sk": "Slovak",
				"sl": "Slovenian",
				"sm": "Samoan",
				"sn": "Shona",
				"so": "Somali",
				"sq": "Albanian",
				"sr": "Serbian",
				"ss": "Swati",
				"st": "Sotho, Southern",
				"su": "Sundanese",
				"sv": "Swedish",
				"sw": "Swahili",
				"ta": "Tamil",
				"te": "Telugu",
				"tg": "Tajik",
				"th": "Thai",
				"ti": "Tigrinya",
				"tk": "Turkmen",
				"tl": "Tagalog",
				"tn": "Tswana",
				"to": "Tonga",
				"tr": "Turkish",
				"ts": "Tsonga",
				"tt": "Tatar",
				"tw": "Twi",
				"ty": "Tahitian",
				"ug": "Uighur",
				"uk": "Ukrainian",
				"ur": "Urdu",
				"uz": "Uzbek",
				"ve": "Venda",
				"vi": "Vietnamese",
				"vo": "Volapük",
				"wa": "Walloon",
				"wo": "Wolof",
				"xh": "Xhosa",
				"yi": "Yiddish",
				"yo": "Yoruba",
				"za": "Zhuang",
				"zh": "Chinese",
				"zu": "Zulu"
};

// user current setting
admin.sub.total = 0; // size of current working set
admin.sub.submissions = []; // current working set
admin.sub.sortBy = 'created';
admin.sub.sortOrder = 'desc';
admin.sub.pageIndex = 1; 
admin.sub.pageSize = 20; 
admin.sub.filterType = 'ALL';

admin.sub.init = function() {
  admin.sub.initSubmissionGrid();  
  admin.sub.initControlPanel();  
  admin.sub.initSubmissionFilters();  
  
  jQuery('#submissionSearchText').keyup( function() {
    admin.sub.filterByText();
  });   
};

admin.sub.initSubmissionFilters = function() {
  var labels = jQuery('#submissionFilters a.filter');
  for(var i=0; i<labels.length; i++) {    
    var label = jQuery(labels[i]);     
    admin.sub.setupLabelFilter(label);
  }  
};

admin.sub.setupLabelFilter = function(label) {
  label.click(function() {    
    
    // reset all label colors
    var labels = jQuery('#submissionFilters a.filter');
    for(var i=0; i<labels.length; i++) {    
      var label_ = jQuery(labels[i]);     
      label_.css('background', 'white');
      label_.css('color', 'black');
    }     
    
    // set the selected label to be highlighted
    label.css('background', '#a6c9e2');
    label.css('color', 'black');     
    
    admin.sub.filterType = label.html();
          
    // reset the page index to first page
    admin.sub.pageIndex = 1;
    
    admin.sub.refreshGrid();                    
    
  });     
  
  if (label.html() == "ALL") {
    label.css('background', '#a6c9e2');
    label.css('color', 'black');      
  }
};

admin.sub.initControlPanel = function() {
  jQuery('#submissionRefreshGrid').click(function() {
    admin.sub.refreshGrid();
  });
  
  jQuery('#submissionNextPage').click(function() {
    admin.sub.pageIndex++;          
    admin.sub.refreshGrid();
  });  
  
  jQuery('#submissionPrevPage').click(function() {
    admin.sub.pageIndex--;          
    admin.sub.refreshGrid();
  });    
};

admin.sub.hasNextPage = function() {
  var totalPages = Math.ceil(admin.sub.total/admin.sub.pageSize);
  if (admin.sub.pageIndex < totalPages) {
    return true;
  } else {
    return false;
  }
};

admin.sub.hasPrevPage = function() {
  if (admin.sub.pageIndex > 1) {
    return true;
  } else {
    return false;
  }
};

admin.sub.filterByText = function() {

  var matches = [];

  var text = jQuery('#submissionSearchText').val();   
  
  var regex = new RegExp(text, 'i');

  for ( var i = 0; i < admin.sub.submissions.length; i++) {
    var entry = admin.sub.submissions[i];

    var title = entry.videoTitle;
    var description = entry.videoDescription;
    var tags = entry.videoTags;

    if (regex.test(title) || regex.test(description) || regex.test(tags)) {
      matches.push(entry);
    }
  }
  
  admin.sub.refreshGridUI(matches); 
}

admin.sub.initSubmissionGrid = function() {
  var grid = {};
  grid.datatype = 'local';
  grid.height = 500;
  grid.multiselect = false;
  grid.pgbuttons = false;  
  grid.caption = 'Submissions';

  grid.cellsubmit = 'clientArray';  
  grid.autowidth = true;  
  grid.cellEdit = true;   
  
  admin.sub.initGridModels(grid);
  
  grid.afterInsertRow = function(rowid, rowdata, rowelem) {
    var entryId = admin.sub.getEntryId(rowid);
    
    var previewButton = jQuery.sprintf('<input type="button" onclick=admin.sub.previewVideo("%s") value="Preview" />', entryId);
    jQuery('#submissionGrid').setCell(rowid, 'preview', previewButton);
    
    var deleteButton = jQuery.sprintf('<input type="button" onclick=admin.sub.deleteEntry("%s") value="Delete" />', entryId);
    jQuery('#submissionGrid').setCell(rowid, 'delete', deleteButton);
    
    var detailsButton = jQuery.sprintf('<input type="button" onclick=admin.sub.fetchDetails("%s") value="Details" />', entryId);
    jQuery('#submissionGrid').setCell(rowid, 'details', detailsButton);     

    if (rowdata['viewCount'] > 0) {
      var viewCountLink = jQuery.sprintf('<a title="Click to download YouTube Insight data." href="/admin/InsightDownloadRedirect?id=%s&user=%s">%s</a>', rowdata['videoId'], rowdata['youTubeName'], rowdata['viewCount']);
      jQuery('#submissionGrid').setCell(rowid, 'viewCount', viewCountLink);
    }   

  };

  grid.afterSaveCell = function(rowid, cellname, value, iRow, iCol) {
    // save entry as JDO    
    var entryId = admin.sub.getEntryId(rowid);
    var submission = admin.sub.getSubmission(entryId);

    if (typeof (submission[cellname]) != 'undefined') {
      submission[cellname] = value;
    }
    admin.sub.updateSubmissionStatus(submission);
  };  
  
  grid.onSortCol = function(colType, columnIndex, sortOrder) {    
    admin.sub.sortBy = colType;
    admin.sub.sortOrder = sortOrder;
    admin.sub.pageIndex = 1;
    admin.sub.refreshGrid();
  };
  
  jQuery('#submissionGrid').jqGrid(grid);
  
  // populate data;
  admin.sub.refreshGrid();
};

admin.sub.initGridModels = function(grid) {
  grid.colNames = [];
  grid.colModel = [];

  grid.colNames.push('Entry ID');
  grid.colModel.push( {
    name : 'id',
    index : 'id',
    width : 100,
    hidden : true,
    sorttype : 'string'
  });
  
  // TODO: Need to write unformatter so jqgrid can sort it, now it's unsortable.
  grid.colNames.push('Created On');
  grid.colModel.push( {
    name : 'created',
    index : 'created',
    width : 100,
    sortype : 'date',
    formatter : function(cellvalue, options, rowObject) {
      var date = new Date(cellvalue);
      return admin.formatDate(date);
    }
  });

  grid.colNames.push('Video ID');
  grid.colModel.push( {
    name : 'videoId',
    index : 'videoId',
    width : 100,
    editable : false,
    hidden : true,
    sorttype : 'string'
  });

  grid.colNames.push('Assignment');
  grid.colModel.push( {
    name : 'assignmentId',
    index : 'assignmentId',
    width : 60,
    hidden : false,
    sorttype : 'string'
  });

  grid.colNames.push('Article');
  grid.colModel.push( {
    name : 'articleUrl',
    index : 'articleUrl',
    width : 50,
    formatter : function(cellvalue, options, rowObject) {
      var link = '';    
      if (cellvalue) { 
        link = '<a title="' + cellvalue + '" href="' + cellvalue + '" target="_blank">link</a>';
      }
      return link;
    },
    align : 'center',
    sorttype : 'string'
  });

  grid.colNames.push('Username');
  grid.colModel.push( {
    name : 'uploader',
    index : 'uploader',
    width : 100,
    hidden : true,
    sorttype : 'string'
  });

  grid.colNames.push('Email');
  grid.colModel.push( {
    name : 'notifyEmail',
    index : 'notifyEmail',
    width : 70,
    hidden : true,
    sorttype : 'string'
  });

  grid.colNames.push('Video Title');
  grid.colModel.push( {
    name : 'videoTitle',
    index : 'videoTitle',
    width : 200,
    sorttype : 'string',
    edittype : 'text'
  });

  grid.colNames.push('Video Description');
  grid.colModel.push( {
    name : 'videoDescription',
    index : 'videoDescription',
    width : 150,
    hidden: true,
    edittype : 'text',
    sorttype : 'string'
  });

  grid.colNames.push('Video Tags');
  grid.colModel.push( {
    name : 'videoTags',
    index : 'videoTags',
    width : 100,
    edittype : 'text',
    sorttype : 'string',
    hidden: true
  });
  
  grid.colNames.push('View Count');
  grid.colModel.push( {
    name : 'viewCount',
    index : 'viewCount',
    width : 80,
    sorttype : 'int',
    hidden: false,
    formatter : function(cellvalue, options, rowObject) {
      if (cellvalue < 0) {
        return 'no data';
      } else {        
        //TODO: Figure out why this needs to be a string value.
        return '' + cellvalue;
      }
    }
  });

  grid.colNames.push('Video Source');
  grid.colModel.push( {
    name : 'videoSource',
    index : 'videoSource',
    width : 110,
    edittype : 'text',
    sorttype : 'string'
  });
  
  grid.colNames.push('Status');
  grid.colModel.push( {
    name : 'status',
    index : 'status',
    width : 100,
    edittype : 'select',
    editable : false,
    editoptions : {
      value : 'UNREVIEWED:UNREVIEWED;APPROVED:APPROVED;REJECTED:REJECTED;SPAM:SPAM'
    },
    sorttype : 'string'
  });
  
  grid.colNames.push('Delete');
  grid.colModel.push( {
    name : 'delete',
    index : 'delete',
    width : 75,
    align : 'center',
    sortable : false,
    hidden: false
  });

  grid.colNames.push('Preview');
  grid.colModel.push( {
    name : 'preview',
    index : 'preview',
    width : 75,
    align : 'center',
    sortable : false
  });

  grid.colNames.push('Download');
  grid.colModel.push( {
    name : 'download',
    index : 'download',
    width : 75,
    align : 'center',
    sortable : false,
    hidden: true
  });

  grid.colNames.push('Details');
  grid.colModel.push( {
    name : 'details',
    index : 'details',
    width : 75,
    align : 'center',
    sortable : false,
    hidden: false
  });
};

admin.sub.getSubmission = function(id) {
  var ret = null;

  for ( var i = 0; i < admin.sub.submissions.length; i++) {
    var submission = admin.sub.submissions[i];
    if (submission.id == id) {
      ret = submission;
      break;
    }
  }

  return ret;
};

admin.sub.getEntryId = function(rowid) {
  return jQuery('#submissionGrid').getCell(rowid, 0);
};

admin.sub.getVideoId = function(rowid) {
  return jQuery('#submissionGrid').getCell(rowid, 2);
};

admin.sub.getTotalPage = function() {
  return Math.ceil(admin.sub.total / admin.sub.pageSize);
};

admin.sub.refreshGrid = function() {
  admin.sub.getAllSubmissions(function(entries) {
    
    admin.sub.refreshGridUI(entries);
    
    var captionTitle = null;
    
    switch(admin.sub.filterType) {
      case 'ALL':
        captionTitle = 'All Submissions';   
        break;
      case 'UNREVIEWED':
        captionTitle = 'Unreviewed Submissions';
        break;
      case 'APPROVED':
        captionTitle = 'Approved Submissions';
        break;
      case 'REJECTED':
        captionTitle = 'Rejected Submissions';
        break;
      case 'SPAM':
        captionTitle = 'Spam Submissions';
        break;        
    }
    
    jQuery('#submissionGrid').setCaption(captionTitle + ' (' + admin.sub.total + ')');      
    
    var totalPage = admin.sub.getTotalPage();
    if (totalPage > 0) {
      jQuery('#submissionPageIndex').html('Page ' + admin.sub.pageIndex + ' of ' + totalPage);
    } else {
      jQuery('#submissionPageIndex').html('0 result');
    }
    
    if (admin.sub.hasNextPage()) {
      jQuery('#submissionNextPage').get(0).disabled = false;
    } else {
      jQuery('#submissionNextPage').get(0).disabled = true;
      
    }

    if (admin.sub.hasPrevPage()) {
      jQuery('#submissionPrevPage').get(0).disabled = false;
    } else {
      jQuery('#submissionPrevPage').get(0).disabled = true; 
    }   
    
  });
};

admin.sub.refreshGridUI = function(entries) {
  var jqGrid = jQuery('#submissionGrid').clearGridData();
  for (var i = 0; i < entries.length; i++) {
    jqGrid.addRowData(i + 1, entries[i]);
  }
};

admin.sub.fetchDetails = function(entryId) {
  var messageElement = admin.showMessage("Loading video details...");

  var command = 'GET_VIDEO_DETAILS';
  var params = {};
  params.submissionId = entryId;

  var jsonRpcCallback = function(json) {
    try {
      if (!json.error) {
        admin.showMessage("Video details loaded.", messageElement);
        admin.sub.showDetails(JSON.parse(json.videoSubmission));
      } else {
        admin.showError(json.error, messageElement);          
      }
    } catch(exception) {
      admin.showError('Request failed: ' + exception, messageElement);
    }
  }

  jsonrpc.makeRequest(command, params, jsonRpcCallback);  
};

admin.sub.showDetails = function(submission) {
  var div = jQuery('<div style="width: 700px; height: 600px;" align="center"/>');
  
  var dialogOptions = {};
  dialogOptions.title = submission.videoTitle;
  dialogOptions.width = 700;
  dialogOptions.height = 700;
  dialogOptions.open = function() {
    var videoWidth = 360;
    var videoHeight = 265;  
  
    var mainDiv = jQuery('#submissionDetailsTemplate').clone();
    mainDiv.css('display', 'block');  
    mainDiv.find('#assignmentId').html(submission.assignmentId);
    
    var created = new Date(submission.created).toLocaleTimeString() + ' ' + new Date(submission.created).toLocaleDateString();
    mainDiv.find('#created').html(created);
    
    mainDiv.find('#videoSource').html(submission.videoSource);  

    var creatorInfo = submission.youTubeName + (submission.notifyEmail ? ' (' + submission.notifyEmail + ')' : '');    
    mainDiv.find('#youTubeName').html(creatorInfo);

    mainDiv.find('#videoId').html(jQuery.sprintf('<a target="_blank" href="http://www.youtube.com/watch?v=%s">%s</a>', submission.videoId, submission.videoId));
    mainDiv.find('#youTubeState').html(submission.youTubeState);
    
    mainDiv.find('#videoTitle').html(submission.videoTitle);

    mainDiv.find('#videoDescription').html(submission.videoDescription);

    mainDiv.find('#videoTags').html(submission.videoTags);  
    
    var articleLink = submission.articleUrl ? jQuery.sprintf('<a target="_blank" href="%s">%s</a>', submission.articleUrl, submission.articleUrl) : 'N/A';  
    mainDiv.find('#articleUrl').html(articleLink);  
    
    mainDiv.find('#videoDate').html(submission.videoDate ? submission.videoDate : 'N/A');
    
    mainDiv.find('#videoLocation').html(submission.videoLocation ? submission.videoLocation : 'N/A');
    
    mainDiv.find('#phoneNumber').html(submission.phoneNumber ? submission.phoneNumber : 'N/A');  
    
    var moderationStatus = -1;
    switch(submission.status) {
      case 'UNREVIEWED':
        moderationStatus = 0;
        break;
      case 'APPROVED':
        moderationStatus = 1;
        break;
      case 'REJECTED':
        moderationStatus = 2;
        break;
      case 'SPAM':
        moderationStatus = 3;
        break;      
    }
      
    mainDiv.find('#moderationStatus').get(0).selectedIndex = moderationStatus;  
    mainDiv.find('#moderationStatus').change(function() {
      switch(mainDiv.find('#moderationStatus').get(0).selectedIndex) {
        case 0:
          submission.status = 'UNREVIEWED';
          break;
        case 1:
          submission.status = 'APPROVED';
          break;
        case 2:
          submission.status = 'REJECTED';
          break;
        case 3:
          submission.status = 'SPAM';
          break;        
      }
      
      if (submission.status == 'APPROVED' && submission.youTubeState != 'OKAY') {
        if (!confirm("This video's state is '" + submission.youTubeState + 
                "'. It may not be available on YouTube. Are you sure you want to approve it?")) {
          return;
        }  
      }
      
      admin.sub.updateSubmissionStatus(submission);
    });    
    
    mainDiv.find('#adminNotes').html(submission.adminNotes);
    
    mainDiv.find('#saveAdminNotes').click(function() {
      var messageElement = admin.showMessage("Saving admin notes...");
      
      var command = 'UPDATE_VIDEO_SUBMISSION_ADMIN_NOTES';
      var params = {};
      params.id = submission.id;
      params.adminNotes = mainDiv.find('#adminNotes').val();
      
      var jsonRpcCallback = function(json) {
        try {
          if (!json.error) {
            admin.showMessage("Admin notes saved.", messageElement);
            submission.adminNotes = params.adminNotes;
          } else {
            admin.showError(json.error, messageElement);
          }
        } catch(exception) {
          admin.showError('Request failed: ' + exception, messageElement);
        }
      } 
      
      jsonrpc.makeRequest(command, params, jsonRpcCallback);
    });
    
    mainDiv.find('#download').click(function() {
      admin.sub.downloadVideo(submission);
    });
    
    mainDiv.find('#captions').click(function() {
      mainDiv.dialog('close');
      admin.sub.loadCaptions(submission.id);
    });  
  
    var videoHtml = admin.sub.getVideoHTML(submission.videoId, videoWidth, videoHeight);
    mainDiv.find('#video').html(videoHtml);
    
    div.append(mainDiv);
  };
  
  jQuery.ui.dialog.prototype.options.bgiframe = true;
  div.dialog(dialogOptions);
};

admin.sub.downloadVideo = function(submission) {
  document.location.href = jQuery.sprintf('/admin/VideoDownloadRedirect?id=%s&username=%s', submission.videoId, submission.youTubeName);
};

admin.sub.loadCaptions = function(submissionId) {
	var messageElement = admin.showMessage("Loading video captions...");
	
  var command = 'GET_YOUTUBE_CAPTIONS';
  var params = {};
  params.submissionId = submissionId;
  
  var jsonRpcCallback = function(json) {
    try {
      if (!json.error) {
      	admin.showMessage("Video captions loaded.", messageElement);
        admin.sub.showCaptionInfo(json);
      } else {
        admin.showError(json.error, messageElement);
      }
    } catch(exception) {
    	admin.showError('Request failed: ' + exception, messageElement);
    }
  }
  
  jsonrpc.makeRequest(command, params, jsonRpcCallback);
};

admin.sub.showCaptionInfo = function(json) {
  var dialogDiv = jQuery('#captionsTemplate').clone();   
  var dialogOptions = {};
  dialogOptions.title = "Edit Captions";
  dialogOptions.width = 650;
  dialogOptions.height = 450;
  
  var languageSelect = dialogDiv.find('#languageSelect');
  
  var options = [];
  for (var languageCode in json.captions) {
		var languageName = admin.sub.languageMap[languageCode] || jQuery.sprintf('Unknown (%s)',
						languageCode);
  	options.push(jQuery.sprintf('<!-- %s --><option id="%s" value="%s">%s</option>',
  					languageName, languageCode, languageCode, languageName));
  }
  if (options.length > 0) {
  	options.sort();
  	languageSelect.append('<optgroup id="existingLanguages" label="Exisiting Languages">' +
  					options.join('\n') + '</optgroup>');
  }
  
  options = [];
  for (var languageCode in admin.sub.languageMap) {
  	if (json.captions[languageCode] == null) {
  		var languageName = admin.sub.languageMap[languageCode] || jQuery.sprintf('Unknown (%s)',
  						languageCode);
  		options.push(jQuery.sprintf('<!-- %s --><option id="%s" value="%s">%s</option>',
  						languageName, languageCode, languageCode, languageName));
  	}
  }
  options.sort();
  languageSelect.append('<optgroup id="availableLanguages" label="Available Languages">' +
  				options.join('\n') + '</optgroup>');
  
  // Select the first item in the available languages list, if there is one.
  var selectedLanguageCode = dialogDiv.find('#availableLanguages').children().val();
  if (selectedLanguageCode) {
  	dialogDiv.find('#' + selectedLanguageCode).attr('selected', 'selected');
  }
  
  languageSelect.change(function() {
  	var selectedLanguageCode = languageSelect.val();
  	if (json.captions[selectedLanguageCode] == null) {
  		dialogDiv.find('#captionTrack').val('');
  	} else {
  		var messageElement = admin.showMessage("Loading " +
  						admin.sub.languageMap[selectedLanguageCode] + " caption track...");
  		
  	  var command = 'GET_YOUTUBE_CAPTION_TRACK';
  	  var params = {};
  	  params.url = json.captions[selectedLanguageCode];
  	  params.username = json.username;
  	  
  	  var jsonRpcCallback = function(json) {
  	    try {
  	      if (!json.error) {
  	      	admin.showMessage("Caption track loaded.", messageElement);
  	      	dialogDiv.find('#captionTrack').val(json.captionTrack);
  	      } else {
  	        admin.showError(json.error, messageElement);
  	      }
  	    } catch(exception) {
  	    	admin.showError('Request failed: ' + exception, messageElement);
  	    }
  	  }
  	  
  	  jsonrpc.makeRequest(command, params, jsonRpcCallback);
  	}
  });
  
  dialogDiv.find('#saveCaption').click(function() {
  	var selectedLanguageCode = languageSelect.val();
  	
		var messageElement = admin.showMessage("Saving " +
						admin.sub.languageMap[selectedLanguageCode] + " caption track...");
		
	  var command = 'UPDATE_YOUTUBE_CAPTION_TRACK';
	  var params = {};
	  params.videoId = json.videoId;
	  params.username = json.username;
	  params.captionTrack = dialogDiv.find('#captionTrack').val();
	  params.languageCode = selectedLanguageCode;
	  
	  var jsonRpcCallback = function(json) {
	    try {
	      if (!json.error) {
	      	if (json.success) {
	      		admin.showMessage("Caption track saved.", messageElement);
	      		dialogDiv.dialog('close');
	      	} else {
	      		admin.showError("The format of your caption track is invalid. Please revise the" +
	      				" caption track to activate it.", messageElement);
	      	}
	      } else {
	        admin.showError(json.error, messageElement);
	      }
	    } catch(exception) {
	    	admin.showError('Request failed: ' + exception, messageElement);
	    }
	  }
	  
	  jsonrpc.makeRequest(command, params, jsonRpcCallback);
  });

  dialogDiv.dialog(dialogOptions);
};

admin.sub.deleteEntry = function(entryId) {
  if (confirm("Delete this entry?")) {
    var messageElement = admin.showMessage("Deleting video submission...");

    var command = 'DELETE_VIDEO_SUBMISSION';
    var params = {};
    params.id = entryId;

    var jsonRpcCallback = function(json) {
      try {
        if (!json.error) {
          admin.showMessage("Video submission deleted.", messageElement);
          admin.sub.refreshGrid();
        } else {
          admin.showError(json.error, messageElement);
        }
      } catch (exception) {
        admin.showError('Request failed: ' + exception, messageElement);
      }
    }

    jsonrpc.makeRequest(command, params, jsonRpcCallback);
  }  
};

admin.sub.previewVideo = function(entryId) {
  var submission = admin.sub.getSubmission(entryId);
  var videoId = submission.videoId;
  var title = submission.videoTitle;

  var videoWidth = 640;
  var videoHeight = 385;
  
  var div = jQuery('<div align="center">');
  
  var dialogOptions = {};
  dialogOptions.title = title;
  dialogOptions.width = videoWidth + 40;
  dialogOptions.height = videoHeight + 50;
  dialogOptions.open = function() {
    div.html(admin.sub.getVideoHTML(videoId, videoWidth, videoHeight));
  };
  
  jQuery.ui.dialog.prototype.options.bgiframe = true;
  div.dialog(dialogOptions);
};

admin.sub.getAllSubmissions = function(callback) {
  var messageElement = admin.showMessage("Loading video submissions...");
  
  var command = 'GET_VIDEO_SUBMISSIONS';
  var params = {};
  params.sortBy = admin.sub.sortBy;
  params.sortOrder = admin.sub.sortOrder;
  params.pageIndex = admin.sub.pageIndex;
  params.pageSize = admin.sub.pageSize;
  params.filterType = admin.sub.filterType;
  
  var jsonRpcCallback = function(json) {
    try {
      if (!json.error) {
        admin.showMessage("Video submissions loaded.", messageElement);
        admin.sub.total = json.totalSize;
        var entries = json.result;
        admin.sub.submissions = entries.concat([]);
        callback(entries);          
      } else {
        admin.showError(json.error, messageElement);          
      }
    } catch(exception) {
    	admin.showError('Request failed: ' + exception, messageElement);
    }
  } 
  
  jsonrpc.makeRequest(command, params, jsonRpcCallback);  
};

admin.sub.updateSubmissionStatus = function(entry) {  
  var messageElement = admin.showMessage("Updating video submission status...");
  
  var command = 'UPDATE_VIDEO_SUBMISSION_STATUS';
  var params = {};
  params.id = entry.id;
  params.status = entry.status;
  
  var jsonRpcCallback = function(json) {
    try {
      if (!json.error) {
        if (json.success) {
          admin.showMessage("Video submission status updated.", messageElement);
        } else {
          admin.showError("Unable to modify YouTube playlist. Please ensure that your YouTube " +
          		"API settings are valid in the Configuration tab.", messageElement);
        }
        admin.sub.refreshGrid();
      } else {         
        admin.showError(json.error, messageElement);      
      }
    } catch(exception) {
    	admin.showError('Request failed: ' + exception, messageElement);
    }
  } 
  
  jsonrpc.makeRequest(command, params, jsonRpcCallback);   
};

admin.sub.getVideoHTML = function(videoId, width, height) {
  var playerWidth = width || 640;
  var playerHeight = height || 385;

  var html = [];
  html.push(jQuery.sprintf('<object width="%d" height="%d">', playerWidth, playerHeight));
  html.push(jQuery.sprintf('<param name="movie" value="http://www.youtube.com/v/%s?fs=1&rel=0&version=3"></param>', videoId));
  html.push('<param name="allowFullScreen" value="true"></param><param name="allowscriptaccess" value="always"></param>');
  html.push(jQuery.sprintf('<embed src="http://www.youtube.com/v/%s?fs=1&rel=0&version=3" type="application/x-shockwave-flash" allowscriptaccess="always" allowfullscreen="true" width="%d" height="%d"></embed>', videoId, playerWidth, playerHeight));
  html.push('</object>');

  return html.join('');
};
