$( document ).ready(function() {

    search('*');

$("#azure-search-bar button").click(function(){

	search($("#azure-search-bar input").val());

});

function search(searchKeyword) {
    var $fields = $('.azure-search.field');

    debugger;

    if ($fields === undefined) {
        console.log("no fields");
        return;
    }

    var query = '';
    $fields.each(function(index) {

        var $type = $(this).data('azure-search-type');
        var $fieldName = $(this).data('azure-search-field-name');
        var queryValue = '';


        if ($type === 'checkbox') {
            var $selectedValue = $(this).find('input:checked');

            $selectedValue.each(function(i) {
                if (queryValue === '') {
                    queryValue = queryValue + $(this).val();
                } else {
                    queryValue = queryValue + '|' + $(this).val();
                }

            });
        } else if($type === 'hidden'){
			queryValue=$(this).data('azure-search-field-value');
        }


        //console.log("Value = "+ queryValue +" $type = "+ $type +" $fieldName = "+ $fieldName);
        if (query === '' && queryValue !== '') {
            query = query + $fieldName + "/any(t:search.in(t, '" + queryValue + "','|'))";
        } else if (queryValue !== '') {
            query = query + ' and ' + $fieldName + "/any(t:search.in(t, '" + queryValue + "','|'))";
        }
    });

    var azuresearchurl = 'https://adobesummit2019qnamaker-aso7skmz7xqhvdi.search.windows.net/indexes/aemcontent/docs?api-version=2019-05-06';

    var url = azuresearchurl + '&$count=true&queryType=full&search=' + searchKeyword;

    if (query!=='' && query.length > 0) {
        url = url + "&$filter=" + query; 
    }

    $('.azure-search-result .outer-loader').show();

    $('.azure-search-result-container').hide();


    $.ajax({
        url: url,
        type: 'GET',
        dataType: 'json',
        headers: {
            'api-key': '7857B3BE261D6950ADD26DB3A54CA6FC'
        },
        contentType: 'application/json; charset=utf-8',
        success: function(result) {
            $("#azure-search-result").empty();

            var totalcountDom = "<br\><div class=\"azuresearch-total-count\">" + result['@odata.count'] + " result(s) found.</div>";
            var resultDom = "";
            for (var i = 0; i < result.value.length; ++i) {
                resultDom = resultDom + "<br/><div class='result-item'><a class='header result' href='" + result.value[i].URL + "'>" + result.value[i].Title + "</a> <div>" + result.value[i].Description + "</div></div>";
            }
            $('.azure-search-result .outer-loader').hide();
            $('.azure-search-result-container').show();

            $("#azure-search-result").append(totalcountDom);
            $("#azure-search-result").append(resultDom);


        },
        error: function(error) {
            alert("Error");
        }
    });
}

$('.azure-search.field input').change(function() {
    var keyword = $("#azure-search-bar input").val();
    if(keyword === '' || keyword === undefined){
        keyword = '*';
    }
  	search(keyword);
});
});