/*
    Use to fix hidden section headers behind the navbar when using links with targets.
    Re-apply on full page load to account for late layout shifts (e.g. images).
*/
(function($) {
  var headerOffset = 60;

  function getTarget(hashValue) {
    if (!hashValue || hashValue === '#') {
      return $();
    }

    var hash = hashValue.charAt(0) === '#' ? hashValue : '#' + hashValue;
    var decodedHash = hash;

    try {
      decodedHash = '#' + decodeURIComponent(hash.slice(1));
    } catch (ignore) {
    }

    var target = $(decodedHash);
    if (!target.length && decodedHash !== hash) {
      target = $(hash);
    }
    if (!target.length) {
      target = $('a.headerlink[href="' + decodedHash.replace(/"/g, '\\"') + '"]');
    }

    return target.first();
  }

  function scrollToHash(hashValue, animate) {
    var target = getTarget(hashValue || window.location.hash);

    if (!target.length) {
      return false;
    }

    var scrollTop = Math.max(target.offset().top - headerOffset, 0);

    if (animate) {
      $('html,body').stop(true).animate({ scrollTop: scrollTop }, 200);
    } else {
      window.scrollTo(0, scrollTop);
    }

    return true;
  }

  $(document).ready(function() {
    $('a[href*="#"]:not([href="#"])').on('click', function(event) {
      var samePath = location.pathname.replace(/^\//, '') === this.pathname.replace(/^\//, '');
      var sameHost = location.hostname === this.hostname;

      if (samePath && sameHost && scrollToHash(this.hash, true)) {
        event.preventDefault();
      }
    });

    if (window.location.hash) {
      scrollToHash(window.location.hash, false);
    }

    $(window).on('hashchange', function() {
      scrollToHash(window.location.hash, false);
    });

    $(window).on('load', function() {
      if (window.location.hash) {
        scrollToHash(window.location.hash, false);
      }
    });
  });
})($jqTheme);
