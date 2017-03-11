{% extends "base.html" %}
{% load crispy_forms_tags %}

{% block content %}
<form class="form-horizontal" action="{% url 'blog:agent-create' %}" method="post">
    {% crispy form %}
    <!--<input type="button" name="save" value="save" class="btn btn-primary" id="submit-id-save">-->
</form>
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.1.1/jquery.min.js" integrity="sha384-3ceskX3iaEnIogmQchP8opvBy3Mi7Ce34nWjpBIwVTHfGYWQS9jwHDVRnpKKHJg7" crossorigin="anonymous"></script>
      
<script>
    $(document).ready(function(){
    // using jQuery
function getCookie(name) {
    var cookieValue = null;
    if (document.cookie && document.cookie !== '') {
        var cookies = document.cookie.split(';');
        for (var i = 0; i < cookies.length; i++) {
            var cookie = jQuery.trim(cookies[i]);
            // Does this cookie string begin with the name we want?
            if (cookie.substring(0, name.length + 1) === (name + '=')) {
                cookieValue = decodeURIComponent(cookie.substring(name.length + 1));
                break;
            }
        }
    }
    return cookieValue;
}
function csrfSafeMethod(method) {
    // these HTTP methods do not require CSRF protection
    return (/^(GET|HEAD|OPTIONS|TRACE)$/.test(method));
}

 $("#submit-id-save").click(function(e){
            console.log("HI")
            e.preventDefault();
            var csrftoken = getCookie('csrftoken');
            $.ajaxSetup({
                beforeSend: function(xhr, settings) {
                    if (!csrfSafeMethod(settings.type) && !this.crossDomain) {
                        xhr.setRequestHeader("X-CSRFToken", csrftoken);
                    }
                }
            });
            var formclass = ".form-horizontal";
            //var form_s = new FormData($(formclass));

            $.ajax({
                url: $(formclass).attr("action"),
                type: 'POST',
                data: $(formclass).serialize(),
                dataType: 'json',
                success: function(data) {
                    if (!(data["success"])) {
                        // Here we replace the form, for the
                        $(formclass).replaceWith(data['form_html']);
                        return false;
                    }
                    else {
                        // Here you can show the user a success message or do whatever you need
                        alert("Success");
                        return false;
                    }
                },
                error: function () {
                    alert("Wrong");
                }
            });
            //$.post($(formclass).attr("action"), form_s);
        });   
    
})

        
   
</script>

{% endblock %}
