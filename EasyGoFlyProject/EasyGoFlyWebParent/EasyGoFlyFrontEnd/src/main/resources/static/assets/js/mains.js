

 (function($) {
     "use strict";


      /*======== Doucument Ready Function =========*/
    jQuery(document).ready(function () {
     //CACHE JQUERY OBJECTS
      $("#status").fadeOut();
      $("#preloader").delay(200).fadeOut("slow");
      $("body").delay(200).css({ "overflow": "visible" });

      
      /* Init Wow Js */
      new WOW().init();

    });

     //search categories
    $('a[href="#search1"]').on('click', function(event) {
         event.preventDefault();
         $('#search1').addClass('open');
         $('#search1 > form > input[type="search"]').focus();
     });
     $('#search1, #search1 button.close').on('click keyup', function(event) {
         if (event.target == this || event.target.className == 'close' || event.keyCode == 27) {
             $(this).removeClass('open');
         }
     });

    jQuery(document).ready(() => {
         jQuery('.js-video-button').modalVideo({
             channel: 'vimeo'
         });
     });

     // Range sliders activation
     $(".range-slider-ui").each(function() {
         var minRangeValue = $(this).attr('data-min');
         var maxRangeValue = $(this).attr('data-max');
         var minName = $(this).attr('data-min-name');
         var maxName = $(this).attr('data-max-name');
         var unit = $(this).attr('data-unit');
         $(this).slider({
             range: true,
             min: minRangeValue,
             max: maxRangeValue,
             values: [minRangeValue, maxRangeValue],
             slide: function(event, ui) {
                 event = event;
                 var currentMin = parseInt(ui.values[0]);
                 var currentMax = parseInt(ui.values[1]);
                 $(this).children(".min-value").text(currentMin + " " + unit);
                 $(this).children(".max-value").text(currentMax + " " + unit);
                 $(this).children(".current-min").val(currentMin);
                 $(this).children(".current-max").val(currentMax);
             }
         });
     });


     /* ------------------------------------------------------------------------ */
     /* BACK TO TOP
    /* ------------------------------------------------------------------------ */
     $(document).on('click', '#back-to-top, .back-to-top', () => {
         $('html, body').animate({
             scrollTop: 0
         }, '500');
         return false;
     });
     $(window).on('scroll', () => {
         if ($(window).scrollTop() > 500) {
             $('#back-to-top').fadeIn(200);
         } else {
             $('#back-to-top').fadeOut(200);
         }
     });

     // Slick SLider
     $('.slider-store').slick({
         slidesToShow: 1,
         slidesToScroll: 1,
         direction: 'vertical',
         arrows: false,
         dots: false,
         fade: true,
         autoplay: true,
         asNavFor: '.slider-thumbs'
     });
    

     $('.slider-thumbs').slick({
         slidesToShow: 5,
         slidesToScroll: 1,
         asNavFor: '.slider-store',
         dots: false,
         arrows: false,
         autoplay: true,
         direction: 'vertical',
         centerMode: true,
         focusOnSelect: true,
         responsive: [{
             breakpoint: 800,
             settings: {
                 arrows:false,
             }
         }]

     });


     $('.review-slider').slick({
         infinite: true,
         slidesToShow: 1,
         slidesToScroll: 1,
         arrows: true,
         dots: false,
         rows:0,
         autoplay: true,
         speed: 2000,
         loop:true,
         responsive: [{
             breakpoint: 991,
             settings: {
                 slidesToShow: 1,
                 arrows: false,
             }
         }]
     });

     $('.review-slider1').slick({
         infinite: true,
         slidesToShow: 2,
         slidesToScroll: 1,
         arrows: false,
         dots: false,
         rows:0,
         autoplay: true,
         speed: 5000,
         loop:true,
         responsive: [{
             breakpoint: 1100,
             settings: {
                 slidesToShow: 1
             }
         }]
     });

     $('.about-slider').slick({
         infinite: true,
         slidesToShow: 1,
         slidesToScroll: 1,
         arrows: false,
         dots: false,
         autoplay: true,
         rows:0,
         speed: 4000,
         loop:true,
         responsive: [{
             breakpoint: 700,
             settings: {
                 arrows:false
             }
         }]
     });

     $('.side-slider').slick({
         infinite: true,
         slidesToShow: 6,
         slidesToScroll: 1,
         arrows: false,
         rows:0,
         dots: false,
         autoplay: true,
         speed: 4000,
         loop:true,
          responsive: [{
             breakpoint: 1000,
             settings: {
                 slidesToShow: 3
             }
         }, 
         {
             breakpoint: 811,
             settings: {
                 slidesToShow: 2
            }
         }, 
         {
             breakpoint: 500,
             settings: {
                 slidesToShow: 1
             }
         }]
     });
     
     $('.side-slider-mobile').slick({
         infinite: true,
         slidesToShow: 6,
         slidesToScroll: 1,
         arrows: false,
         rows:0,
         dots: false,
         autoplay: true,
         speed: 4000,
         loop:true,
          responsive: [{
             breakpoint: 1000,
             settings: {
                 slidesToShow: 3
             }
         }, 
         {
             breakpoint: 811,
             settings: {
                 slidesToShow: 3
            }
         }, 
         {
             breakpoint: 500,
             settings: {
                 slidesToShow: 2
             }
         }]
     });

      $('.attract-slider').slick({
         infinite: true,
         slidesToShow: 8,
         slidesToScroll: 1,
         arrows: false,
         dots: false,
         speed: 2000,
         rows:0,
         autoplay: true,
         draggable:false,
         responsive: [{
             breakpoint: 1000,
             settings: {
                 slidesToShow: 4
             }
         }, 
         {
             breakpoint: 600,
             settings: {
                 slidesToShow: 3
            }
         }, 
         {
             breakpoint: 500,
             settings: {
                 slidesToShow: 2
             }
         }]
     });

    
     $('.team-slider').slick({
         infinite: true,
         slidesToShow: 3,
         slidesToScroll: 1,
         arrows: false,
         dots: true,
         autoplay: true,
         speed: 1000,
         rows:0,
         loop:true,
         responsive: [{
             breakpoint: 1000,
             settings: {
                 slidesToShow: 2
             }
         }, {
             breakpoint: 750,
             settings: {
                 slidesToShow: 1
             }
         }]
     });

     $('.item-slider').slick({
         infinite: true,
         slidesToShow: 3,
         slidesToScroll: 1,
         arrows: true,
         dots: false,
         autoplay: true,
         speed: 2000,
         rows:0,
         loop:true,
         responsive: [{
             breakpoint: 1000,
             settings: {
                 slidesToShow: 2,
                 arrows: false,
             }
         }, {
             breakpoint: 750,
             settings: {
                 slidesToShow: 1,
                 arrows: false,
             }
         }]
     });
     
     $('.item-slider2').slick({
         infinite: false,
         slidesToShow: 1,
         slidesToScroll: 1,
         arrows: true,
         appendArrows: $('.search-tags'),
         prevArrow: `<button  class="me-2 nir-btn-default nir-btn-search-type white py-1 nir-btn-toggler" type="button" onclick="return changeSerachClass(this);">Domestic</button >`,
         nextArrow: `<button  class="ms-2 nir-btn-default nir-btn-search-type white py-1" type="button" onclick="return changeSerachClass(this);">International</button>`,
         dots: false,
         autoplay: false,
         speed: 800,
         rows:0,
         loop:true,
         responsive: [{
             breakpoint: 1000,
             settings: {
                 slidesToShow: 1,
                 arrows: false,
             }
         }, {
             breakpoint: 750,
             settings: {
                 slidesToShow: 1,
                 arrows: false,
             }
         }]
     });
     
     $('.item-slider3').slick({
         infinite: false,
         slidesToShow: 1,
         slidesToScroll: 1,
         arrows: true,
         autoplay: false,
         appendArrows: $('.search-mobile-tag'),
         prevArrow: `<button  class="me-2 nir-btn-default nir-btn-search-type white py-1 nir-btn-toggler domestic-btn" type="button" onclick="return changeSerachClass(this);">Domestic</button >`,
         nextArrow: `<button  class="ms-2 nir-btn-default nir-btn-search-type white py-1 international-btn" type="button" onclick="return changeSerachClass(this);">International</button>`,
         dots: false,
         speed: 800,
         rows: 0,
         loop:true,
         responsive: [{
             breakpoint: 1000,
             settings: {
                 slidesToShow: 1,
                 arrows: true,
             }
         }, {
             breakpoint: 750,
             settings: {
                 slidesToShow: 1,
                 arrows: true,
             }
         }]
     });

     $('.item-slider1').slick({
         infinite: true,
         slidesToShow: 3,
         slidesToScroll: 1,
         arrows: false,
         dots: false,
         autoplay: true,
         speed: 2000,
         rows:0,
         loop:true,
         responsive: [{
             breakpoint: 1000,
             settings: {
                 slidesToShow: 1,
                 arrows: false,
             }
         }, {
             breakpoint: 750,
             settings: {
                 slidesToShow: 1,
                 arrows: false,
             }
         }]
     });

     $('.side-slider-frequent').slick({
         infinite: true,
         slidesToShow: 6,
         slidesToScroll: 1,
         arrows: false,
         rows:0,
         dots: false,
         autoplay: true,
         speed: 5000,
         loop:true,
          responsive: [{
             breakpoint: 1000,
             settings: {
                 slidesToShow: 3
             }
         }, 
         {
             breakpoint: 811,
             settings: {
                 slidesToShow: 3
            }
         }, 
         {
             breakpoint: 500,
             settings: {
                 slidesToShow: 2
             }
         }]
     });
     $('.banner-slider').slick({
         infinite: true,
         slidesToShow: 4,
         slidesToScroll: 1,
         arrows: true,
         dots: false,
         autoplay: true,
         speed: 2000,
         rows:0,
         cursor: false,
         loop:true,
         responsive: [{
             breakpoint: 1000,
             settings: {
                 slidesToShow: 2
             }
         }, {
             breakpoint: 800,
             settings: {
                 slidesToShow: 1
             }
         }]
     });

     $('.shop-slider').slick({
         infinite: true,
         slidesToShow: 4,
         slidesToScroll: 1,
         arrows: false,
         dots: false,
         autoplay: true,
         speed: 2000,
         rows:0,
         cursor: false,
         loop:true,
         responsive: [{
             breakpoint: 1000,
             settings: {
                 slidesToShow: 2
             }
         }, {
             breakpoint: 800,
             settings: {
                 slidesToShow: 1
             }
         }]
     });

     // Slick Testimonial Slider
        $('.sl-testimonial-slider').slick({
          slidesToShow: 1,
          slidesToScroll: 1,
          vertical: true,
          verticalSwiping: true,
          autoplay: true,
          Speed: 8000,
          rows:0,
          infinite: true,
          arrows: false,
          dots: false,
          adaptiveHeight: true
        });

     $('.partner-slider').slick({
         infinite: true,
         slidesToShow: 5,
         slidesToScroll: 1,
         arrows: false,
         dots: false,
         autoplay: true,
         speed: 2000,
         rows:0,
         loop:true,
         responsive: [{
             breakpoint: 1000,
             settings: {
                 slidesToShow: 3
             }
         }, {
             breakpoint: 800,
             settings: {
                 slidesToShow: 2
             }
         }, {
             breakpoint: 500,
             settings: {
                 slidesToShow: 1
             }
         }]
     });


     $("#contactform2").validate({      
      submitHandler: function() {
        
        $.ajax({
          url : 'mail/contact.php',
          type : 'POST',
          data : {
            fname : $('input[name="first_name"]').val(),
            lname : $('input[name="last_name"]').val(),
            email : $('input[name="email"]').val(),
            phone : $('input[name="phone"]').val(),
            comments : $('textarea[name="comments"]').val(),
          },
          success : function( result ){
            $('#contactform-error-msg').html( result );
            $("#contactform2")[0].reset();
          }     
        });

      }
    });

    
     /*-----------------------------------------------------------------------------------*/
    /*  COUNTDOWN
    /*-----------------------------------------------------------------------------------*/

     $(document).ready(() => {
         loopcounter('coming-counter');
     });

    /*-----------------------------------------------------------------------------------*/
    /*  COUNTER UP
    /*-----------------------------------------------------------------------------------*/
    $('.value').counterUp({
        delay: 50,
        time: 1000
    });
    /*-----------------------------------------------------------------------------------*/
    /*  MASONRY
    /*-----------------------------------------------------------------------------------*/
    
     $('.blog-main').masonry({
         // options
         itemSelector: '.mansonry-item',
     });

     $('.trend-box1').masonry({
         // options
         itemSelector: '.mansonry-item1',
     });

     // Nice Select JS
     $('.niceSelect').niceSelect();

     $('a[href="#search1"]').on('click', function(event) {
         event.preventDefault();
         $('#search1').addClass('open');
         $('#search1 > form > input[type="search"]').focus();
     });
     $('#search1, #search1 button.close').on('click keyup', function(event) {
         if (event.target == this || event.target.className == 'close' || event.keyCode == 27) {
             $(this).removeClass('open');
         }
     });
     
	/*================================================================
          19. menu toggle
  =================================================================*/
	$(".toggle-btn").on("click", function () {
		$(".m-menu").addClass("show");
		$(".m-menu__overlay").addClass("show");
	});
	$(".m-menu__close, .m-menu__overlay").on("click", function () {
		$(".m-menu").removeClass("show");
		$(".m-menu__overlay").removeClass("show");
	});
	
     
	/*================================================================
          20. read more
  =================================================================*/
  
  	var newHtml = `<p class="dots-p">...</p>
  										<p class="dots-p-s d-none" style="height: 24px;"></p>
                    		<div class="d-flex justify-content-center dots-b">
								<button class="btn btn-primary p-2 px-4" style="width: fit-content;">Read more</button>
							</div>`;
							
  	$(".read-main").append(newHtml);

	// Handle button click for toggling behavior
	$(".dots-b button").click(function () {
	    var $button = $(this);
	    var $readMain = $button.parents(".read-main");
	    var $readMore = $readMain.children(".read-more");
	    var $dotsP = $readMain.find(".dots-p");
	    var $dotsPS = $readMain.find(".dots-p-s");
	
	    if ($readMore.hasClass("expanded")) {
	        // Collapse logic
	        $dotsP.removeClass("d-none"); // Show dots
	        $dotsPS.addClass("d-none"); // Hide dot space
	        $readMore.removeClass("expanded").css("height", "250px");
	        $button.text("Read more");
	    } else {
	        // Expand logic
	        var itemHeight = $readMore.children(".read-more-item").height() + "px";
	        $dotsP.addClass("d-none"); // Hide dots
	        $dotsPS.removeClass("d-none"); // Show dot space
	        $readMore.addClass("expanded").css("height", itemHeight);
	        $button.text("Read less");
	    }
	});




 })(jQuery);


 jQuery(window).on('resize load', () => {
     resize_eb_slider();
 }).resize();
 /**
  * Resize slider
  */

 function resize_eb_slider() {
     let bodyheight = jQuery(this).height();
     if (jQuery(window).width() > 1400) {
         bodyheight *= 0.90;
         jQuery('.slider').css('height', `${bodyheight}px`);
     }
 }
 
function convertToInt(str) {
	var arr = str.split("px");
	return parseInt(arr[0]);
}

