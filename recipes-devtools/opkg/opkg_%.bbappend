do_install_append_class-target () {
    # Include pre-configure opkg feeds
    if [ ! -z "${IPK_INCLUDE_FEEDS}" ]; then
        for feed in ${IPK_INCLUDE_FEEDS}; do
            feed_name=$(echo ${feed} | cut -d ',' -f 1)
            feed_url=$(echo ${feed} | cut -d ',' -f 2)
            echo "src/gz $feed_name $feed_url" >>${D}${sysconfdir}/opkg/opkg.conf
        done
    fi
}
