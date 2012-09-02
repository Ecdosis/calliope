<?php
include("hrit.php");
if ( extension_loaded("hrit") )
	echo "hrit extension loaded, Sir!\n";
else
	echo "HRIT extension NOT loaded, Sir!\n";
if ( extension_loaded("vector") )
	echo "vector extension loaded, Sir!\n";
else
	echo "vector extension NOT loaded, Sir!\n";
$vfuncs = get_extension_funcs('hrit'); 
foreach ($vfuncs as $func )
{
	echo "$func\n";
}
$hrit = new hrit_formatter("hello world",11);
if ( $hrit )
	echo "created hrit_formatter instance, Sir!\n";
?>
