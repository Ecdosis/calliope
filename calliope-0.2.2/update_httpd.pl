open HTTPDIN, "<$ARGV[0]" or die "couldn't open ".$ARGV[0];
open HTTPDOUT, ">/tmp/httpd.conf" or die "couldn't open httpd.conf";
$found_proxy=0;
$found_balancer=0;
$found_http=0;
$started=0;
while (<HTTPDIN>)
{
    if ( /^LoadModule/ )
    {
        $started=1;
        if ( /proxy_module/ )
        {
            $found_proxy=1;
        }
        elsif ( /proxy_http_module/ )
        {
            $found_http=1;
        }
        elsif ( /proxy_balancer_module/ )
        {
            $found_balancer=1;
        }
    }
    elsif ( $started && !/^#/ )
    {
        if ( !$found_proxy )
        {
            print HTTPDOUT "LoadModule proxy_module libexec/apache2/mod_proxy.so\n";
            $found_proxy=1;
        }
        if ( !$found_balancer )
        {
            print HTTPDOUT "LoadModule proxy_balancer_module libexec/apache2/mod_proxy_balancer.so\n";
            $found_balancer=1;
        }
        if ( !$found_http )
        {
            print HTTPDOUT "LoadModule proxy_http_module libexec/apache2/mod_proxy_http.so\n";
            $found_http=1;
        }
        $started=0;
    }
    print HTTPDOUT $_;
}
close HTTPDOUT;
close HTTPDIN;
rename "/tmp/httpd.conf", $ARGV[0];
